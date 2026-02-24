import React, { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [indices, setIndices] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const today = "2026년 2월 23일";

  useEffect(() => {
    fetch('http://localhost:8080/api/briefing/indices')
      .then(response => response.json())
      .then(data => {
        setIndices(data);
        setIsLoading(false);
      })
      .catch(error => {
        console.error('Error fetching indices:', error);
        setIsLoading(false);
      });
  }, []);

  const summaryItems = [
    "엔비디아의 실적 발표 이후 기술주 중심의 강력한 매수세가 유입되었습니다.",
    "연준 위원들의 매파적 발언에도 불구하고 AI 산업에 대한 기대감이 시장을 주도했습니다.",
    "달러 인덱스는 소폭 하락하며 위험 자산 선호 심리가 개선되는 모습을 보였습니다."
  ];

  return (
    <div className="container">
      <header className="header">
        <p className="date">{today}</p>
        <h1 className="title">오늘의 미국 증시 브리핑</h1>
      </header>

      <section className="section">
        <div className="card summary-card">
          <div className="card-header">
            <span className="ai-badge">AI 요약</span>
            <h3>밤사이 핵심 요약</h3>
          </div>
          <ul className="summary-list">
            {summaryItems.map((item, index) => (
              <li key={index}>{item}</li>
            ))}
          </ul>
        </div>
      </section>

      <section className="section">
        <h2 className="section-title">주요 지수</h2>
        {isLoading ? (
          <p>로딩 중...</p>
        ) : (
          <div className="index-grid">
            {indices.map((index, i) => {
              const isUp = index.changeRate.startsWith('+');
              return (
                <div key={i} className="card index-card">
                  <span className="index-name">{index.name}</span>
                  <div className="index-value">{index.value}</div>
                  <div className={`index-change ${isUp ? 'up' : 'down'}`}>
                    {index.changeRate}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      <section className="section">
        <div className="card news-card">
          <h2 className="section-title">주요 뉴스</h2>
          <div className="news-item">
            <p className="news-tag">반도체</p>
            <p className="news-text">엔비디아, 시가총액 2조 달러 돌파 눈앞...</p>
          </div>
          <div className="news-item">
            <p className="news-tag">경제지표</p>
            <p className="news-text">신규 실업수당 청구 건수 예상치 하회...</p>
          </div>
        </div>
      </section>

      <footer className="footer">
        <button className="notify-button">
          매일 아침 알림 받기
        </button>
      </footer>
    </div>
  )
}

export default App
