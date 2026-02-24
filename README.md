
---

# 📈 Vibe Pro: AI 미국 증시 브리핑 서비스

**Vibe Pro**는 실시간 미국 증시 지수와 주요 뉴스를 수집하고, Google Gemini AI를 활용해 밤사이의 핵심 이슈를 한눈에 요약해 주는 풀스택 웹 애플리케이션입니다.

## 🚀 Key Features

* **Real-time Market Indices:** S&P 500, NASDAQ, Dow Jones 등 주요 지수 시각화.
* **Daily News Curation:** 미국 시장의 핵심 뉴스 피드 제공.
* **AI Summary:** Gemini 2.5 Flash 모델을 연동하여 복잡한 뉴스 데이터를 3줄 핵심 요약으로 제공.
* **Responsive UI:** React 기반의 깔끔하고 직관적인 대시보드 인터페이스.

## 🛠 Tech Stack

### Frontend

* **React (Vite)**
* **CSS3** (Custom Styling)

### Backend

* **Java 17 / Spring Boot 3.x**
* **Spring Data JPA**
* **Google Gemini API** (AI Analysis)

### Infrastructure & DevOps

* **Cloud:** AWS EC2 (Amazon Linux 2023)
* **Web Server:** Nginx (Reverse Proxy)
* **CI/CD:** Manual Deployment (GitHub Actions & Terraform 예정)
* **Certifications:** AWS Solutions Architect - Associate (SAA) 기준 인프라 설계

## 🏗 System Architecture

서비스는 AWS EC2 인스턴스 내에서 **Nginx-Backend-Frontend** 구조로 운영됩니다.

1. **Nginx (Port 80):** 클라이언트 요청을 수신하여 정적 파일(React)을 서빙하고, `/api` 요청을 백엔드로 프록시 전달합니다.
2. **Spring Boot (Port 8080):** 외부 API로부터 데이터를 수집하고 Gemini AI를 통해 요약 로직을 수행합니다.
3. **Security:** AWS 보안 그룹(Security Group)을 통해 80(HTTP), 8080(API) 포트를 제어하며, API Key는 환경 변수로 격리하여 관리합니다.

## ⚙️ Environment Variables

보안을 위해 API 키 및 민감 정보는 환경 변수로 관리합니다.

## 🌐 Deployment (AWS)

현재 본 프로젝트는 AWS EC2 환경에 배포되어 있습니다.

* **Instance:** Amazon Linux 2023 (t2.micro)
* **Reverse Proxy:** Nginx configuration for `/api` routing.
* **Process Management:** `nohup` & `systemd` service for background execution.

---

### 💡 Future Roadmap (DevOps)

* [ ] **IaC:** Terraform을 이용한 AWS 인프라 구축 자동화.
* [ ] **Configuration Management:** Ansible을 통한 서버 설정 및 배포 자동화.
* [ ] **CI/CD Pipeline:** GitHub Actions를 활용한 무중단 배포 시스템 구축.

---

