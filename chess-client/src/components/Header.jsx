import React from 'react';

export default function Header({ username, elo, onLogout }) {
  return (
    <header style={{display:'flex',justifyContent:'space-between',alignItems:'center',padding:'12px 20px',background:'#fff',borderBottom:'1px solid #e6eef8'}}>
      <div style={{display:'flex',alignItems:'center',gap:12}}>
        <div style={{width:40,height:40,background:'#111827',color:'#fff,',borderRadius:8,display:'flex',alignItems:'center',justifyContent:'center'}}>CM</div>
        <div>
          <div style={{fontWeight:700}}>{username || 'ChessMaster'}</div>
          <div style={{fontSize:12,color:'#6b7280'}}>ELO {elo||'-'}</div>
        </div>
      </div>
      <div>
        <button onClick={onLogout} style={{padding:'8px 12px',borderRadius:8,background:'#eef4ff',color:'#1152d4',border:0}}>Logout</button>
      </div>
    </header>
  );
}


