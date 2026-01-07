import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../api/auth';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [err, setErr] = useState('');
  const navigate = useNavigate();

  async function submit() {
    setErr('');
    try {
      const data = await login(username, password);
      const token = data.token;
      localStorage.setItem('token', token);
      sessionStorage.setItem('token', token);
      navigate('/lobby');
    } catch (e) {
      setErr(e.message || 'Đăng nhập thất bại');
    }
  }

  return (
    <div className="auth-wrap">
      <div className="card auth-card">
        <div className="auth-brand">
          <div style={{ width: 80, height: 80, background: 'var(--accent)', borderRadius: 20, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 800, fontSize: 32, boxShadow: '0 0 30px rgba(99, 102, 241, 0.3)' }}>♟︎</div>
          <h2 style={{ marginTop: 24, marginBottom: 8, fontSize: 24 }}>Chess Game</h2>
          <p style={{ color: 'var(--text-dim)', textAlign: 'center', fontSize: 14, lineHeight: 1.6 }}>Trang web cờ vua trực tuyến</p>
        </div>
        <div className="auth-form">
          <h1 style={{ margin: '0 0 8px', fontSize: 28 }}>Đăng nhập</h1>
          <p style={{ color: 'var(--text-dim)', marginTop: 0, marginBottom: 32 }}>Chào mừng bạn quay trở lại!</p>
          <div style={{ display: 'grid', gap: 16 }}>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: 'var(--text-dim)', fontWeight: 600 }}>TÊN ĐĂNG NHẬP</label>
              <input style={{ width: '100%' }} placeholder="Nhập tên đăng nhập" value={username} onChange={e => setUsername(e.target.value)} />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: 'var(--text-dim)', fontWeight: 600 }}>MẬT KHẨU</label>
              <input style={{ width: '100%' }} placeholder="••••••••" type="password" value={password} onChange={e => setPassword(e.target.value)} />
            </div>
            <button onClick={submit} className="primary" style={{ marginTop: 8, height: 48 }}>Đăng nhập ngay</button>
            {err && <div style={{ color: 'var(--danger)', fontSize: 14, fontWeight: 500, textAlign: 'center' }}>{err}</div>}
            <div style={{ marginTop: 16, color: 'var(--text-dim)', textAlign: 'center', fontSize: 14 }}>
              Chưa có tài khoản? <Link to="/register" style={{ color: 'var(--accent)', fontWeight: 600, textDecoration: 'none' }}>Đăng ký ngay</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}


