import React, { useEffect, useState } from 'react';
import { getProfile, getMatches } from '../api/user';
import { useNavigate } from 'react-router-dom';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [matches, setMatches] = useState([]);
  const nav = useNavigate();

  useEffect(() => {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    if (!token) { nav('/login'); return; }
    getProfile(token).then(setProfile).catch(() => { });
    getMatches(token).then(setMatches).catch(() => { });
  }, [nav]);

  if (!profile) return <div className="site-container">Đang tải thông tin cá nhân...</div>;

  return (
    <div className="site-container">
      <div className="lobby-header">
        <h1>Hồ sơ của tôi</h1>
        <button onClick={() => nav('/lobby')} className="secondary">Quay lại Sảnh chờ</button>
      </div>

      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
          <div style={{ width: 80, height: 80, background: 'var(--accent)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 32, fontWeight: 800 }}>
            {profile.username[0].toUpperCase()}
          </div>
          <div>
            <h2 style={{ margin: 0 }}>{profile.username}</h2>
            <div style={{ color: 'var(--accent)', fontSize: 18, fontWeight: 700 }}>Điểm ELO: {profile.elo}</div>
          </div>
        </div>

        <div className="stats-grid">
          <div className="stat-item card" style={{ padding: 16 }}>
            <div className="stat-value">{profile.totalGames || 0}</div>
            <div className="stat-label">Tổng trận đã chơi</div>
          </div>
          <div className="stat-item card" style={{ padding: 16, borderLeft: '4px solid #22c55e' }}>
            <div className="stat-value" style={{ color: '#22c55e' }}>{profile.wins || 0}</div>
            <div className="stat-label">Trận thắng</div>
          </div>
          <div className="stat-item card" style={{ padding: 16, borderLeft: '4px solid #ef4444' }}>
            <div className="stat-value" style={{ color: '#ef4444' }}>{profile.losses || 0}</div>
            <div className="stat-label">Trận thua</div>
          </div>
          <div className="stat-item card" style={{ padding: 16, borderLeft: '4px solid #94a3b8' }}>
            <div className="stat-value" style={{ color: '#94a3b8' }}>{profile.draws || 0}</div>
            <div className="stat-label">Trận hòa</div>
          </div>
        </div>
      </div>

      <h3 style={{ marginTop: 32 }}>Lịch sử đấu</h3>
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table style={{ width: '100%' }}>
          <thead>
            <tr>
              <th>Đối thủ</th>
              <th>Kết quả</th>
              <th>Phe chơi</th>
              <th>Thay đổi ELO</th>
              <th>Ngày thi đấu</th>
            </tr>
          </thead>
          <tbody>
            {matches.map(m => {
              const diff = m.eloAfter - m.eloBefore;
              return (
                <tr key={m.matchId}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{m.opponent}</div>
                  </td>
                  <td>
                    <span className={`result-tag tag-${m.result.toLowerCase()}`}>
                      {m.result === 'WIN' ? 'THẮNG' : (m.result === 'LOSS' ? 'THUA' : 'HÒA')}
                    </span>
                  </td>
                  <td style={{ color: 'var(--text-dim)' }}>{m.color === 'WHITE' ? 'TRẮNG' : 'ĐEN'}</td>
                  <td style={{ fontWeight: 700, color: diff > 0 ? '#4ade80' : (diff < 0 ? '#f87171' : 'var(--text-dim)') }}>
                    {diff > 0 ? `+${diff}` : diff}
                  </td>
                  <td style={{ color: 'var(--text-dim)', fontSize: 13 }}>
                    {new Date(m.playedAt).toLocaleDateString()}
                  </td>
                </tr>
              );
            })}
            {matches.length === 0 && (
              <tr><td colSpan="5" style={{ textAlign: 'center', padding: 40, color: 'var(--text-dim)' }}>Bạn chưa tham gia trận đấu nào.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}


