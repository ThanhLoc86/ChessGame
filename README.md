# ChessGame

Một ứng dụng cờ vua online full-stack với giao diện React và backend Spring Boot.

## Kiến trúc hệ thống

- **Frontend**: React + Vite
- **Backend**: Spring Boot với WebSocket và JWT
- **Database**: MySQL
- **Chess Engine**: Java module

## Hướng dẫn Deployment

### Tùy chọn 1: Railway + Vercel (Khuyến nghị cho người mới)

#### 1. Chuẩn bị Database (Railway)
```bash
# Tạo tài khoản tại https://railway.app
# Tạo project mới và thêm MySQL database
# Sao chép DATABASE_URL từ Railway
```

#### 2. Deploy Backend (Railway)
```bash
# Cập nhật application.properties cho production
# Trong chess-server/src/main/resources/application.properties:
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

#### 3. Deploy Frontend (Vercel)
```bash
# Cài đặt Vercel CLI
npm install -g vercel

# Trong thư mục chess-client
npm install
npm run build

# Deploy lên Vercel
vercel --prod
```

### Tùy chọn 2: Heroku (Đơn giản)

#### 1. Chuẩn bị Heroku
```bash
# Cài đặt Heroku CLI
# Tạo tài khoản Heroku
heroku create your-chess-app-backend
```

#### 2. Cấu hình Database
```bash
# Thêm Heroku Postgres add-on
heroku addons:create heroku-postgresql:hobby-dev

# Cập nhật application.properties
spring.datasource.url=${JDBC_DATABASE_URL}
spring.jpa.hibernate.ddl-auto=update
```

#### 3. Deploy Backend
```bash
# Trong thư mục chess-server
heroku buildpacks:add heroku/java
git init
git add .
git commit -m "Initial commit"
heroku git:remote -a your-chess-app-backend
git push heroku main
```

#### 4. Deploy Frontend
```bash
# Sử dụng Heroku cho frontend hoặc Vercel
# Trong chess-client
npm install
npm run build
# Sau đó upload thư mục dist lên hosting
```

### Tùy chọn 3: DigitalOcean App Platform

#### 1. Tạo Droplet với Docker
```yaml
# docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: chess_game
    volumes:
      - mysql_data:/var/lib/mysql

  backend:
    build: ./chess-server
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/chess_game
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: your_password

volumes:
  mysql_data:
```

#### 2. Deploy lên DigitalOcean
- Tạo tài khoản DigitalOcean
- Sử dụng App Platform hoặc Droplet
- Upload code và cấu hình

## Cấu hình Environment Variables

### Backend (.env)
```
DATABASE_URL=jdbc:mysql://host:port/database
DB_USER=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

### Frontend (.env)
```
VITE_API_BASE_URL=https://your-backend-url.com/api
VITE_WS_URL=wss://your-backend-url.com/ws
```

## Kiểm tra sau khi Deploy

1. ✅ Frontend load được
2. ✅ Đăng ký/đăng nhập hoạt động
3. ✅ WebSocket kết nối được
4. ✅ Tạo phòng chơi được
5. ✅ Chơi cờ real-time được

## Troubleshooting

### Lỗi CORS
- Thêm cấu hình CORS trong Spring Boot
- Cập nhật allowed origins

### Lỗi Database Connection
- Kiểm tra DATABASE_URL format
- Đảm bảo timezone settings

### Lỗi WebSocket
- Kiểm tra WS URL configuration
- Verify backend WebSocket config