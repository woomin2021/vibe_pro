import React, { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [indices, setIndices] = useState([]);
  const [news, setNews] = useState([]);
  const [summary, setSummary] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingNews, setIsLoadingNews] = useState(true);
  const [isSummaryLoading, setIsSummaryLoading] = useState(true);
  const today = "2026년 2월 23일";

    useEffect(() => {
        // 1. 주요 지수 데이터 가져오기
        fetch("/api/briefing/indices") // 엔드포인트 확인!
            .then(res => res.json())
            .then(data => setIndices(data))
            .catch(err => console.error('Error fetching indices:', err));

        // 2. 뉴스 목록 가져오기
        fetch("/api/briefing/news")
            .then(res => res.json())
            .then(data => {
                setNews(data);
                setIsLoadingNews(false); // 👈 이 줄이 있는지 확인! 없으면 추가해 주세요.
            })
            .catch(err => {
                console.error('Error fetching news:', err);
                setIsLoadingNews(false); // 👈 에러 나도 로딩은 꺼줘야 함
            });

        // 3. AI 요약 데이터 가져오기
        setIsSummaryLoading(true);
        fetch("/api/briefing/summary")
            .then(res => res.json())
            .then(data => {
                setSummary(data);
                setIsSummaryLoading(false);
            })
            .catch(err => {
                console.error('Error fetching summary:', err);
                setIsSummaryLoading(false);
            });

        setIsLoading(false); // 전체 로딩 종료
    }, []);

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
            {isSummaryLoading ? (
              <p style={{ fontSize: '14px', color: '#64748b' }}>AI가 밤사이 뉴스를 분석하고 있습니다...</p>
            ) : (
              summary.map((item, index) => (
                <li key={index}>{item}</li>
              ))
            )}
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
          {isLoadingNews ? (
            <p>뉴스를 불러오는 중...</p>
          ) : (
            news.map((item, index) => (
              <div key={index} className="news-item">
                <a 
                  href={item.link} 
                  target="_blank" 
                  rel="noopener noreferrer" 
                  className="news-text"
                >
                  {item.title}
                </a>
                <p className="news-date">
                  {item.pubDate}
                </p>
              </div>
            ))
          )}
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
