<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!DOCTYPE html>
<html ng-app="jakdukApp">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<jsp:include page="../include/html-header.jsp"/>
<style type="text/css">
.photoHolder {
    display: inline-block;
    border: 4px solid #fff;
    text-align: center;
    box-sizing: border-box;
}

.photoClass {
    display: inline-block;
    background: #eee;
    box-sizing: border-box;    
    background-position: center center;
    background-repeat: no-repeat;
    background-size: cover;
    width: 100%;
    height: 100%;
    border: 4px solid #dfe2e2;
}

.caption1 {
    overflow: hidden;
    font-size: 9px;
    white-space: nowrap;
    text-overflow: ellipsis;
}
</style>
</head>
<body>
<div class="container jakduk">
<jsp:include page="../include/navigation-header.jsp"/>

<div id="imageGalleryPage" ng-controller="GalleryCtrl">    
    <div id="imageGallery">
        <div class="photoHolder" ng-repeat="photo in photos" ng-attr-style="width: {{thumbWidth}}px; height: {{thumbWidth}}px;">
            <div class="photoClass thumbnail" data-id="{{photo.id}}" ng-attr-style="background-image:url({{zoomSize < 5 ? photo.thumb_url : photo.medium_url}})"></div>
            <div class="caption1">{{photo.caption}}</div>
            <div class="caption1">{{photo.date}}</div>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-4 col-sm-2 col-md-2 col-lg-2" ng-repeat="photo in photos">
        	<a href="#" class="thumbnail">
        		<img ng-src="{{photo.thumb_url}}">
        		<div class="caption">
        		dddd
        		<p><small>aaaa</small></p>
        		</div>
        	</a>
        </div>
    </div>    
</div>



<script src="<%=request.getContextPath()%>/resources/jquery/js/jquery.min.js"></script>
<script type="text/javascript">

function rand(min, max) {
	  return Math.round(Math.random() * (max - min) + min);
	}

var jakdukApp = angular.module("jakdukApp", []);

jakdukApp.controller('GalleryCtrl', function($scope, $http) {
    var caption = '';
    var photos = [];
    
    var tmp = new Date(1310000000000 + (rand(0,2317410007)));
    var dateString = tmp.toDateString();
    
    photos.push({id: 0, date: dateString, caption: "test01",
        thumb_url : '<%=request.getContextPath()%>/gallery/thumbnail/54cb15d0e4b0030ea3c1dfe6',
        medium_url : '<%=request.getContextPath()%>/gallery/54cb15d0e4b0030ea3c1dfe6'
     });

    photos.push({id: 1, date: dateString, caption: "test02",
        thumb_url : '<%=request.getContextPath()%>/gallery/thumbnail/54d07584e4b0ce01a5aaf1be',
        medium_url : '<%=request.getContextPath()%>/gallery/54d07584e4b0ce01a5aaf1be'
     });

    photos.push({id: 2, date: dateString, caption: "test03",
        thumb_url : '<%=request.getContextPath()%>/gallery/thumbnail/54cb15d0e4b0030ea3c1dfe6',
        medium_url : '<%=request.getContextPath()%>/gallery/54cb15d0e4b0030ea3c1dfe6'
     });
    
    photos.push({id: 3, date: dateString, caption: "test04",
        thumb_url : '<%=request.getContextPath()%>/gallery/thumbnail/54cb15d0e4b0030ea3c1dfe6',
        medium_url : '<%=request.getContextPath()%>/gallery/54cb15d0e4b0030ea3c1dfe6'
     });      
    
    
    $scope.photos = photos;
    $scope.zoomSize = 4;
    $scope.thumbWidth = 100;
    
    window.onresize = function(){
        $scope.$apply();
    };
    $scope.query = '';    
    $scope.orderProp = 'caption'
});
</script>

<jsp:include page="../include/body-footer.jsp"/>
</body>
</html>