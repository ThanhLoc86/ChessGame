import React, { useState, useEffect, useRef } from 'react';

export default function ChatBox({ messages, onSendMessage }) {
    const [text, setText] = useState('');
    const endRef = useRef(null);

    useEffect(() => {
        endRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const handleSend = (e) => {
        e.preventDefault();
        if (text.trim()) {
            onSendMessage(text);
            setText('');
        }
    };

    return (
        <div className="chat-container card">
            <div className="chat-messages">
                {messages.map((m, i) => (
                    <div key={i} className="chat-message">
                        <span className="chat-sender">{m.sender}: </span>
                        <span className="chat-text">{m.text}</span>
                    </div>
                ))}
                <div ref={endRef} />
            </div>
            <form onSubmit={handleSend} className="chat-input-wrap">
                <input
                    type="text"
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    placeholder="Nhập nội dung..."
                    className="chat-input"
                />
                <button type="submit" className="chat-send-btn">Gửi</button>
            </form>
        </div>
    );
}
