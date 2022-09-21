$(function(){
	$("#sendBtn").click(send_letter);
	$("#delMsg").click(delete_letter);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val()
	var content = $("#message-text").val()

	$.post(
		CONTEXT_PATH + "/letter/send",
		{
			"toName" : toName,
			"content" : content
		},
		function (data){
			data = $.parseJSON(data)
			if(data.code == 0){
				$("#hintBody").text("发送成功")
			}
			else{
				$("#hintBody").text(data.msg)
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 1000);
		}
	);
}
function delete_letter() {
	//TODO: 删除消息无响应
	var id = $("#delete_id");
	$.post(
		CONTEXT_PATH + '/letter/del',
		{
			"id" : id
		},
		function (data){
			data = $.parseJSON(data);
			if(data.code == 0){
				console.log("删除成功");
			} else {
				console.log(data.msg);
			}

			// 1秒后，自动隐藏提示框
			setTimeout(function(){
				if(data.code == 0){
					window.location.reload();
				}
			}, 1000);
		}

	);
}
