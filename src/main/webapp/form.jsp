<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<div class="container">
	<h3>
		<c:out value="${action}" />
		book
	</h3>

	<form method="POST" action="${destination}"
		enctype="multipart/form-data">
		<div class="form-group">
			<label for="title">Title</label> <input type="text" name="title"
				id="title" value="${fn:escapeXml(book.title)}" class="form-control" />
		</div>
		<div class="form-group">
			<label for="author">Author</label> <input type="text" name="author"
				id="author" value="${fn:escapeXml(book.author)}"
				class="form-control" />
		</div>
		<div class="form-group">
			<label for="publishedDate">Date Published</label> <input type="text"
				name="publishedDate" id="publishedDate"
				value="${fn:escapeXml(book.publishedDate)}" class="form-control" />
		</div>
		<div class="form-group">
			<label for="description">Description</label>
			<textarea name="description" id="description" class="form-control">${fn:escapeXml(book.description)}</textarea>
		</div>
		<div class="form-group ${isCloudStorageConfigured ? '' : 'hidden'}">
			<label for="image">Cover Image</label> <input type="file" name="file"
				id="file" class="form-control" />
		</div>
		<div class="form-group hidden">
			<label for="imageUrl">Cover Image URL</label> <input type="hidden"
				name="id" value="${book.id}" /> <input type="text" name="imageUrl"
				id="imageUrl" value="${fn:escapeXml(book.imageUrl)}"
				class="form-control" />
		</div>
		<button type="submit" class="btn btn-success">Save</button>
	</form>
</div>
