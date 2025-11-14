<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>오류 발생</title>
    <link rel="icon" type="image/x-icon" href="/favicon_05.ico"/>

    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f8f9fa;
            color: #343a40;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
            text-align: center;
        }

        .error-container {
            background-color: #ffffff;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
            width: 600px;
            max-width: 90%;
            text-align: center;
            box-sizing: border-box;
        }
        .error-details {
            font-size: 0.9em;
            color: #6c757d;
            margin-top: 20px;
            text-align: left;
            border-top: 1px solid #e9ecef;
            padding-top: 15px;
        }
        .error-details strong {
            display: inline-block;
            width: 120px;
        }

        h1 {
            color: #dc3545; /* Bootstrap danger color */
            font-size: 1.8em;
            margin-bottom: 20px;
        }

        p {
            font-size: 1.1em;
            /*line-height: 1.6;*/
            /*margin-bottom: 15px;*/
            margin: 8px 0px 8px 0px;
            line-height: 120%;
        }

        a {
            color: #007bff;
            text-decoration: none;
            font-weight: 500;
        }
        a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <h1>오류가 발생했습니다!</h1>
        <p>요청하신 페이지를 처리하는 도중 문제가 발생했습니다.</p>
        <p>잠시 후 다시 시도하거나, 담당 관리자에게 문의해주세요.</p><br>

        <p><a href="/">로그인 페이지로 돌아가기</a></p>

        <c:if test="${not empty status}"><!-- 오류 상세 정보 표시 -->
            <div class="error-details">
                <strong>HTTP 상태: </strong><c:out value="${status}"/><br>
                <strong>오류 메시지: </strong><c:out value="${error}"/><br>
                <c:if test="${not empty message}">
                    <strong>상세 설명: </strong><c:out value="${message}"/><br>
                </c:if>
                <c:if test="${not empty timestamp}">
                    <strong>시간: </strong><c:out value="${timestamp}"/>
                </c:if>
            </div>
        </c:if>
    </div>
</body>
</html>
