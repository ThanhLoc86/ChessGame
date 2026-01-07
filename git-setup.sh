#!/bin/bash

echo "🚀 Chuẩn bị push Chess Game lên GitHub"
echo "========================================"

# Kiểm tra git status
echo "📊 Kiểm tra trạng thái Git hiện tại..."
git status

# Khởi tạo git repository nếu chưa có
if [ ! -d ".git" ]; then
    echo "📝 Khởi tạo Git repository..."
    git init
fi

# Thêm tất cả files (trừ những file trong .gitignore)
echo "➕ Thêm tất cả files..."
git add .

# Commit đầu tiên
echo "💾 Tạo commit đầu tiên..."
git commit -m "Initial commit: Chess game online full-stack

- Frontend: React + Vite
- Backend: Spring Boot + WebSocket + JWT
- Database: MySQL
- Chess Engine: Java library

Features:
- User authentication & registration
- Real-time chess gameplay
- Chat system
- Game history & ELO rating
- Responsive UI

Deployment ready for Railway + Vercel"

# Hướng dẫn kết nối với GitHub
echo ""
echo "🔗 Hướng dẫn kết nối với GitHub:"
echo "1. Tạo repository mới trên GitHub: https://github.com/new"
echo "2. Repository name: chess-game-online"
echo "3. Chạy lệnh sau (thay YOUR_USERNAME bằng username GitHub của bạn):"
echo ""
echo "git remote add origin https://github.com/YOUR_USERNAME/chess-game-online.git"
echo "git branch -M main"
echo "git push -u origin main"
echo ""
echo "🎉 Sau đó project sẽ được push lên GitHub và sẵn sàng deploy!"

echo ""
echo "📋 Checklist trước khi push:"
echo "✅ Đã tạo .gitignore"
echo "✅ Đã xóa files nhạy cảm"
echo "✅ Đã commit tất cả code cần thiết"
echo "✅ Sẵn sàng deploy lên Railway + Vercel"
