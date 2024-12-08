<template>
    <footer class="footer">
        <div class="footer-content">
            <div class="company-name">
                <p>ABOUT NANUKKO</p>
            </div>

            <div class="company-info">
                <p>대표이사 : 윤여운, 신희원</p>
                <p>개인정보보호책임자 : 이효승, 김은강</p>
                <p>사업자등록번호 : 123-456-7890</p>
                <p>주소 : 서울특별시 강남구 삼성로 534</p>
                <p>문의 : nanukko@naver.com</p>
            </div>

            <div class="copyright">
                <p>&copy; nanukko.inc All rights reserved.</p>
            </div>
        </div>
    </footer>
</template>
<style scoped>
.footer {
    width: 100%;
    padding: 2rem 0;
    border-top: 1px solid #e0e0e0;
    display: flex;
    justify-content: center;
}


.footer-content {
    width: 100%;
    text-align: center;
    padding: 0 2rem;
}

.company-name {
    margin-bottom: 1rem;
    font-size: 0.9rem;
    color: #333;
    font-weight: bold;
}

.company-info {
    margin: 1rem 0;
}

.company-info p {
    margin: 0.3rem 0;
    color: #666;
    font-size: 0.8rem;
    line-height: 1.4;
}

.copyright {
    margin-top: 1rem;
    font-size: 0.8rem;
    color: #888;
}
</style>