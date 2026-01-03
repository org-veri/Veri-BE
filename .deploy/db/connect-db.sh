#!/usr/bin/env bash
set -euo pipefail

CONFIG="${CONFIG:-./tunnels.yaml}"

command -v yq >/dev/null 2>&1 || { echo "ERROR: yq is required"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "ERROR: jq is required"; exit 1; }
command -v lsof >/dev/null 2>&1 || { echo "ERROR: lsof is required"; exit 1; }

PIDS=()

cleanup() {
  for pid in "${PIDS[@]:-}"; do
    kill "$pid" 2>/dev/null || true
  done
}
trap cleanup EXIT INT TERM

# 터널 목록 순회: 서브쉘 방지 (중요)
while read -r t; do
  NAME="$(jq -r '.name' <<<"$t")"
  SSH_HOST="$(jq -r '.ssh_host' <<<"$t")"
  LP="$(jq -r '.local_port' <<<"$t")"
  RH="$(jq -r '.remote_host' <<<"$t")"
  RP="$(jq -r '.remote_port' <<<"$t")"

  # 필수값 체크
  [[ -n "$NAME" && "$NAME" != "null" ]] || { echo "ERROR: name is missing"; exit 1; }
  [[ -n "$SSH_HOST" && "$SSH_HOST" != "null" ]] || { echo "ERROR: ssh_host is missing for $NAME"; exit 1; }
  [[ -n "$LP" && "$LP" != "null" ]] || { echo "ERROR: local_port is missing for $NAME"; exit 1; }
  [[ -n "$RH" && "$RH" != "null" ]] || { echo "ERROR: remote_host is missing for $NAME"; exit 1; }
  [[ -n "$RP" && "$RP" != "null" ]] || { echo "ERROR: remote_port is missing for $NAME"; exit 1; }

  # 기존 포트 점유 프로세스 정리(ssh만 죽이도록 제한)
  if lsof -tiTCP:"$LP" -sTCP:LISTEN >/dev/null 2>&1; then
    while read -r pid; do
      comm="$(ps -p "$pid" -o comm= 2>/dev/null | tr -d ' ')"
      if [[ "$comm" == "ssh" ]]; then
        kill "$pid" 2>/dev/null || true
      else
        echo "ERROR: port $LP is in use by non-ssh process (pid=$pid, comm=$comm) for $NAME"
        exit 1
      fi
    done < <(lsof -tiTCP:"$LP" -sTCP:LISTEN 2>/dev/null)
  fi

  # SSH 터널 오픈
  ssh -o ExitOnForwardFailure=yes \
      -o ServerAliveInterval=30 \
      -o ServerAliveCountMax=3 \
      -N -L "${LP}:${RH}:${RP}" \
      "${SSH_HOST}" &
  pid=$!
  PIDS+=("$pid")

  # 터널이 실제로 LISTEN 상태가 될 때까지 대기(최대 5초)
  ok=0
  for _ in {1..50}; do
    if lsof -iTCP:"$LP" -sTCP:LISTEN -n -P >/dev/null 2>&1; then
      ok=1
      break
    fi
    # ssh가 바로 죽었는지 체크
    if ! kill -0 "$pid" 2>/dev/null; then
      break
    fi
    sleep 0.1
  done

  if [[ "$ok" -ne 1 ]]; then
    echo "ERROR: tunnel failed: $NAME (localhost:$LP -> $RH:$RP via $SSH_HOST)"
    exit 1
  fi

  echo "Tunnel up: ${NAME} localhost:${LP} -> ${RH}:${RP} via ${SSH_HOST}"
done < <(yq -o=json '.tunnels' "$CONFIG" | jq -c '.[]')

# 실행할 커맨드가 없으면 터널 유지, 있으면 커맨드 실행 후 종료 시 정리(trap)
if [[ $# -eq 0 ]]; then
  echo "No command given. Tunnels are running; Ctrl+C to stop."
  wait "${PIDS[@]}"
else
  "$@"
fi
