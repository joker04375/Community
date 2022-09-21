$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// 获取标题和内容
	var title = $("#recipient-name").val()
	var content = $("#message-text").val()

	// 发送异步请求
	$.post(
		CONTEXT_PATH + "/discussPost/add",
		{
			"title" : title,
			"content" : content
		},
		function(data){
			console.log(data)
			data = $.parseJSON(data);
			// 在提示框中显示返回消息
			$("#hintBody").text(data.msg);

			// 显示提示框
			$("#hintModal").modal("show");

			// 1秒后，自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 刷新页面
				if(data.code == 0){
					window.location.reload();
				}
			}, 1000);
		}

	);

}