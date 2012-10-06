<?php
require('config.php');
?>
<!DOCTYPE HTML>
<html lang="en-US">
<head>
<meta charset="UTF-8">
<title>Twip 4 - Configuration</title>
<link rel="stylesheet" type="text/css" href="style.css" media="all" />
<meta name="robots" content="noindex,nofollow">
</head>
<body>
	<h1><a href="index.html">Twip<sup title="Version 4">4</sup></a></h1>
	<h2>Twitter API Proxy, redefined.</h2>
	<div>
	
		<h3>你的 API Proxy 地址</h3>
		
		<p>
            <input readonly="readonly" type="text" value="<?php echo isset($_GET['api']) ? $_GET['api'] : BASE_URL.'t/'; ?>" onmouseover="this.focus()" onfocus="this.select()" autocomplete="off" />
		</p>

        <?php if(isset($_GET['api'])) { ?>
        <h3>你的 Image Proxy 地址</h3>

        <p>
            <input readonly="readonly" type="text" value="<?php echo str_replace('/o/','/i/',$_GET['api']); ?>" onmouseover="this.focus()" onfocus="this.select()" autocomplete="off" />
        </p>
        <?php } ?>
		
		<p>
			友情提醒：请不要随意泄漏你的 API 地址。Twip 默认会保护你的 API 地址不被搜索引擎爬取。
		</p>
		
<?php
if(isset($_GET['api'])){
?>
		<p> <!--O mode only-->
			注意： O 模式下每次提交认证都会生成新的随机 API 地址！
		</p>
<?php
}
?>
		
		<p class="clearfix">
			<a class="button" href="index.html">返回首页</a>
		</p>
		
	</div>		
	<div id="footer">
		2012 &copy; <a href="http://code.google.com/p/twip/">Twip Project</a>
	</div>
</body>
</html>
