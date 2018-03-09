<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html lang="en">
<head>
<title>Bookshelf - Java on Google Cloud Platform</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
</head>
<body>
	<div class="navbar navbar-default">
		<div class="container">
			<div class="navbar-header">
				<div class="navbar-brand">Bookshelf</div>
			</div>
			<ul class="nav navbar-nav">
				<li><a href="/">Books</a></li>
				<c:if test="${isAuthConfigured}">
					<li><a href="/books/mine">My Books</a></li>
				</c:if>
			</ul>
			<p class="navbar-text navbar-right">
				<c:choose>
					<c:when test="${not empty token}">
						<!-- using pageContext requires jsp-api artifact in pom.xml -->
						<a href="/logout"> <c:if test="${not empty userImageUrl}">
								<img class="img-circle" src="${fn:escapeXml(userImageUrl)}"
									width="24">
							</c:if> ${fn:escapeXml(userEmail)}
						</a>
					</c:when>
					<c:when test="${isAuthConfigured}">
						<a href="/login">Login</a>
					</c:when>
				</c:choose>
			</p>
		</div>
	</div>
	<c:import url="/${page}.jsp" />
</body>
</html>
