# Sổ Tay Học Tập: Lập Trình Mạng & Đa Luồng (Dự án ChessGame)

Tài liệu này tổng hợp các kiến thức lý thuyết và cách hiện thực thực tế trong project ChessGame để giúp bạn ôn tập và nắm vững các khái niệm quan trọng.

---

## 1. Kiến Thức Lý Thuyết

### 1.1. Mô Hình (Models)
- **Client-Server Model**: Project sử dụng mô hình Máy khách - Máy chủ.
    - **Server (Java/Spring Boot)**: Đóng vai trò trung tâm xử lý logic bàn cờ, quản lý các phòng chơi (Rooms) và điều phối tin nhắn giữa các người chơi.
    - **Client (React/Vite)**: Giao diện người dùng, gửi các bước đi và nhận trạng thái bàn cờ mới từ Server.
- **Stateful vs Stateless**: Đây là một ứng dụng **Stateful**. Server lưu trữ trạng thái của ván cờ (Board State) trong bộ nhớ trong suốt quá trình kết nối WebSocket được duy trì.

### 1.2. Giao Thức (Protocols)
- **WebSocket (TCP-based)**: Giao thức chính được dùng để giao tiếp thời gian thực.
    - Khác với HTTP (Request-Response), WebSocket cung cấp kết nối song công (Full-duplex), cho phép Server chủ động gửi dữ liệu về Client mà không cần đợi yêu cầu.
    - Chạy trên nền tảng **TCP**, đảm bảo dữ liệu đến đúng thứ tự và không bị mất mát (cực kỳ quan trọng trong trò chơi chiến thuật như Cờ vua).
- **JSON (JavaScript Object Notation)**: Định dạng dữ liệu dùng để trao đổi giữa Client và Server.

### 1.3. Lớp Xử Lý (Processing Layers)
- **Presentation Layer (Client)**: Hiển thị bàn cờ, xử lý thao tác kéo thả quân cờ.
- **Transport Layer**: Xử lý việc đóng gói và gửi nhận gói tin (WebSocket Handlers).
- **Business/Application Logic Layer (Server)**: Kiểm tra nước đi hợp lệ, chiếu tướng, hết cờ (nằm trong `chess-engine` và `service`).
- **Data Layer**: Lưu trữ kết quả ván đấu vào cơ sở dữ liệu (Repository layer). 

---

## 2. Áp Dụng Vào Thực Tế (Hiện Thực)

### 2.1. Luồng Nhập Xuất (Input/Output Streams)
Trong lập trình mạng hiện đại với Spring Boot, chúng ta ít khi thao tác trực tiếp với `InputStream`/`OutputStream` thô. Thay vào đó, chúng ta sử dụng các thư viện SerDe (Serializer/Deserializer):
- **Hiện thực**: Sử dụng thư viện **Jackson** (`ObjectMapper`) để chuyển đổi luồng văn bản JSON thành Object Java và ngược lại.
- **File quan trọng**: [ChessGameWebSocketHandler.java](file:///d:/Downloads/BAOCAODOAN_Nhom9/BAOCAODOAN_Nhom9/ChessGame/ChessGame/chess-server/src/main/java/com/chessgame/chessserver/ws/ChessGameWebSocketHandler.java)
    - `mapper.readTree(payload)`: Đọc luồng dữ liệu vào.
    - `mapper.writeValueAsString(message)`: Chuẩn bị luồng dữ liệu ra.

### 2.2. TCP / UDP Socket
- **TCP (WebSocket)**: Project sử dụng WebSocket. Bản chất WebSocket là một bản nâng cấp từ HTTP Handshake lên một kết nối TCP bền vững.
- **UDP**: Không được sử dụng trong project này vì Cờ vua yêu cầu độ tin cậy tuyệt đối (không được mất nước đi), trong khi UDP ưu tiên tốc độ nhưng cho phép mất gói tin.

### 2.3. Multicast (Mô phỏng Broadcast)
Mặc dù không dùng `MulticastSocket` của IP Multicast, nhưng logic ứng dụng thực hiện việc phát tin cho một nhóm (Group/Room):
- **Hiện thực**: Phương thức `sendToBoth(String text)` trong `GameRoom`.
- **Nguyên lý**: Duy trì danh sách các `WebSocketSession` trong một phòng và dùng vòng lặp để gửi tin nhắn cho tất cả thành viên trong nhóm đó.

### 2.4. Đa Tuyến (Multithreading/Đa luồng)
Hệ thống phải xử lý nhiều người chơi cùng lúc, mỗi kết nối WebSocket có thể chạy trên các luồng khác nhau.
- **Thread Safety**: Sử dụng `ConcurrentHashMap` trong `GameManager` để tránh xung đột khi nhiều người chơi tạo/vào phòng cùng lúc.
- **Đồng bộ hóa (Synchronization)**:
    - Từ khóa `synchronized` trong `GameRoom.addPlayer` và `GameRoom.applyEngineMove` để đảm bảo tại một thời điểm chỉ có một luồng được thay đổi trạng thái bàn cờ hoặc thêm người chơi.
    - Từ khóa `volatile` cho các biến `playerWhiteSession` để đảm bảo giá trị mới nhất luôn được đọc đúng giữa các luồng.

---

## 3. Các Hàm & Chương Trình Quan Trọng

| Hàm / Thành phần | Vị trí | Mô tả |
| :--- | :--- | :--- |
| `handleTextMessage` | `ChessGameWebSocketHandler` | "Cửa ngõ" tiếp nhận mọi gói tin từ mạng, phân loại (Move, Create, Join). |
| `joinRoom` | `GameManager` | Quản lý logic gán người chơi vào các luồng xử lý (phòng cờ) riêng biệt. |
| `applyEngineMove` | `GameRoom` | Thực thi logic nghiệp vụ (Cờ vua) và cập nhật trạng thái chung. |
| `sendToBoth` | `GameRoom` | Đóng vai trò là bộ phát tín hiệu (Broadcaster) cho những người tham gia. |

> [!TIP]
> **Điểm cần lưu ý khi học**:
> 1. Cách Server ánh xạ (mapping) giữa một `WebSocketSession` với một định danh người chơi trong Database.
> 2. Cách xử lý ngoại lệ (`try-catch`) khi mạng bị ngắt đột ngột trong `sendToBoth`.
