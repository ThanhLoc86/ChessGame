import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../api/auth';

export default function Register() {
  const [u, setU] = useState(''); const [p, setP] = useState(''); const [p2, setP2] = useState('');
  const [err, setErr] = useState('');
  const nav = useNavigate();
  async function submit() {
    if (p !== p2) { setErr('Mật khẩu không khớp'); return; }
    try {
      await register(u, p);
      nav('/login');
    } catch (e) { setErr(e.message || 'Đăng ký thất bại'); }
  }
  return (
    <div className="auth-wrap">
      <div className="card auth-card">
        <div className="auth-form">
          <h1 style={{ margin: '0 0 8px', fontSize: 28 }}>Tạo tài khoản</h1>
          <p style={{ color: 'var(--text-dim)', marginTop: 0, marginBottom: 32 }}>Tham gia cộng đồng kỳ thủ của chúng tôi.</p>
          <div style={{ display: 'grid', gap: 16 }}>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: 'var(--text-dim)', fontWeight: 600 }}>TÊN ĐĂNG NHẬP</label>
              <input style={{ width: '100%' }} placeholder="Chọn tên đăng nhập" value={u} onChange={e => setU(e.target.value)} />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: 'var(--text-dim)', fontWeight: 600 }}>MẬT KHẨU</label>
              <input style={{ width: '100%' }} placeholder="••••••••" type="password" value={p} onChange={e => setP(e.target.value)} />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: 'var(--text-dim)', fontWeight: 600 }}>XÁC NHẬN MẬT KHẨU</label>
              <input style={{ width: '100%' }} placeholder="••••••••" type="password" value={p2} onChange={e => setP2(e.target.value)} />
            </div>
            <button onClick={submit} className="primary" style={{ marginTop: 8, height: 48 }}>Đăng ký tài khoản</button>
            {err && <div style={{ color: 'var(--danger)', fontSize: 14, fontWeight: 500, textAlign: 'center' }}>{err}</div>}
            <div style={{ marginTop: 16, color: 'var(--text-dim)', textAlign: 'center', fontSize: 14 }}>
              Đã có tài khoản? <Link to="/login" style={{ color: 'var(--accent)', fontWeight: 600, textDecoration: 'none' }}>Đăng nhập</Link>
            </div>
          </div>
        </div>
        <div className="auth-brand" style={{ borderRight: 'none', borderLeft: '1px solid rgba(255, 255, 255, 0.05)' }}>
          <div style={{ width: 80, height: 80, background: 'var(--secondary)', borderRadius: 20, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 800, fontSize: 32, border: '1px solid var(--glass-border)' }}>♟︎</div>
          <h2 style={{ marginTop: 24, marginBottom: 8, fontSize: 24 }}>Chess Royale</h2>
          <p style={{ color: 'var(--text-dim)', textAlign: 'center', fontSize: 14, lineHeight: 1.6 }}>Đưa kỹ năng của bạn lên một tầm cao mới với hệ thống xếp hạng công bằng.</p>
        </div>
      </div>
    </div>
  );
}


