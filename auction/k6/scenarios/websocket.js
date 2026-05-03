/**
 * WebSocket 동시 연결 테스트 (STOMP over WebSocket)
 * 목적: 입찰 실시간 브로드캐스트 스케일 확인
 *       - N개 클라이언트가 동시 구독
 *       - 입찰 발생 시 메시지 수신 확인
 *
 * 실행: k6 run auction/k6/scenarios/websocket.js -e CONNECTIONS=200 --out json=results/ws.json
 *
 * 주의: k6/ws는 표준 WebSocket만 지원. STOMP 핸드셰이크를 수동으로 구현.
 *       STOMP 연결 실패 시 ws-stomp 라이브러리 추가 검토 필요.
 */
import ws from 'k6/ws';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { WS_URL } from '../config/thresholds.js';
import { SEED_AUCTIONS } from '../helpers/data.js';

const messagesReceived = new Counter('ws_messages_received');
const connectionErrors = new Rate('ws_connection_error_rate');

const TARGET_CONNECTIONS = parseInt(__ENV.CONNECTIONS || '100');
const AUCTION_ID = SEED_AUCTIONS.ONGOING;

export const options = {
  scenarios: {
    websocket_connections: {
      executor: 'constant-vus',
      vus: TARGET_CONNECTIONS,
      duration: '3m',
    },
  },
  thresholds: {
    ws_connection_error_rate: ['rate<0.05'],
    ws_messages_received: ['count>0'],
  },
};

export default function () {
  const sessionId = uuidv4();

  const res = ws.connect(WS_URL, {}, function (socket) {
    socket.on('open', () => {
      // STOMP CONNECT 프레임
      socket.send(
        'CONNECT\naccept-version:1.2\nheart-beat:0,0\n\n\x00'
      );
    });

    socket.on('message', (msg) => {
      if (msg.startsWith('CONNECTED')) {
        // STOMP SUBSCRIBE 프레임
        socket.send(
          `SUBSCRIBE\nid:sub-${sessionId}\ndestination:/topic/auctions/${AUCTION_ID}\n\n\x00`
        );
      } else if (msg.startsWith('MESSAGE')) {
        messagesReceived.add(1);
      }
    });

    socket.on('error', (e) => {
      connectionErrors.add(1);
    });

    // 최대 2분간 연결 유지
    socket.setTimeout(() => {
      socket.send('DISCONNECT\n\n\x00');
      socket.close();
    }, 120000);
  });

  check(res, {
    'WebSocket 연결 성공': (r) => r && r.status === 101,
  });
}
