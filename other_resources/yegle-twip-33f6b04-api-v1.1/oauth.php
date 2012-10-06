<?php
session_start();
require('include/twitteroauth.php');
require('config.php');
if(isset($_POST['url_suffix'])){
    $_SESSION['url_suffix'] = preg_replace('/[^a-zA-Z0-9]/','',$_POST['url_suffix']);
}
if(!empty($_POST)){
    if(!isset($_GET['type']) || $_GET['type']==1 || $_GET['type']==2){
        $connection = new TwitterOAuth(OAUTH_KEY, OAUTH_SECRET);
        $request_token = $connection->getRequestToken(BASE_URL.'oauth.php');

        /* Save request token to session */
        $_SESSION['oauth_token'] = $request_token['oauth_token'];
        $_SESSION['oauth_token_secret'] = $request_token['oauth_token_secret'];

        switch ($connection->http_code) {
          case 200:
            /* Build authorize URL */
            $url = $connection->getAuthorizeURL($_SESSION['oauth_token'],FALSE);
            if ($_GET['type']==1 || !isset($_GET['type'])) {
                header('HTTP/1.1 302 Found');
                header('Status: 302 Found');
                header('Location: ' . $url); 
            } else {
                // encode user and password for decode.
                $encUser = base64_encode($_POST['username']);
                $encPass = base64_encode($_POST['password']);
                header('HTTP/1.1 302 Found');
                header('Status: 302 Found');
                header('Location: oauth_proxy.php?u=' . $encUser . '&p=' . $encPass . '&g=' . urlencode($url));
            }
            break;
          default:
            echo 'Could not connect to Twitter. Refresh the page or try again later.';
            echo "\n Error code:".$connection->http_code.".";
            if($connection->http_code==0){
                echo "Don't report bugs or issues if you got this error code. Twitter is not accessible on this host. Perhaps the hosting company blocked Twitter.";
            }
            break;
        }
    }
    exit();
}
if(isset($_GET['oauth_token']) && isset($_GET['oauth_verifier'])){
    $connection = new TwitterOAuth(OAUTH_KEY, OAUTH_SECRET, $_SESSION['oauth_token'], $_SESSION['oauth_token_secret']);
    $access_token = $connection->getAccessToken($_GET['oauth_verifier']);
    if($connection->http_code == 200){
        $old_tokens = glob('oauth/*.'.$access_token['screen_name']);
        if(!empty($old_tokens)){
            foreach($old_tokens as $file){
                unlink($file);
            }
        }
        if($_SESSION['url_suffix']==''){
            for ($i=0; $i<6; $i++) {
                $d=rand(1,30)%2;
                $suffix_string .= $d ? chr(rand(65,90)) : chr(rand(48,57));
            } 
        }
        else{
            $suffix_string = $_SESSION['url_suffix'];
        }
        if(file_put_contents('oauth/'.$suffix_string.'.'.$access_token['screen_name'],serialize($access_token)) === FALSE){
            echo 'Error failed to write access_token file.Please check if you have write permission to oauth/ directory'."\n";
            exit();
        }
        $url = BASE_URL.'o/'.$suffix_string;
        header('HTTP/1.1 302 Found');
        header('Status: 302 Found');
        header('Location: getapi.php?api='.$url);
    }
    else {
        echo 'Error '.$connection->http_code."\n";
        print_r($connection);
    }
    exit();
}
?>
<!DOCTYPE HTML>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<title>Twip 4 - Configuration</title>
<link rel="stylesheet" type="text/css" href="style.css" media="all" />
<meta name="robots" content="noindex,nofollow">
</head>
<body>
	<h1><a href="index.html">Twip<sup title="Version 4">4</sup></a></h1>
	<h2>Twitter API Proxy, redefined.</h2>
<?php
if(!isset($_GET['type']) || $_GET['type']==1){
?>
	<div>
	
		<h3>Twip 配置</h3>
		
		<form action="" method="post">
		
			<ul class="clearfix">
				<li><a class="active" href="oauth.php?type=1">OAuth 验证</a></li>
				<li><a href="oauth.php?type=2">模拟 OAuth 验证</a></li>
			</ul>
			
			<hr class="clear" />
			
			<p>
				<label for="url_suffix">自定义 URL 地址</label>
                <input class="half" type="text" value="<?php echo BASE_URL.'o/';?>" id="base_url" disabled autocomplete="off" />
				<input class="half" type="text" value="" id="url_suffix" autocomplete="off" name="url_suffix" />
			</p>
			
			<input type="submit" value="提交认证" class="button">
		
		</form>
	</div>		
	<div id="footer">
		2012 &copy; <a href="http://code.google.com/p/twip/">Twip Project</a>
	</div>
</body>
</html>
<?php
}
else{
?>
	<div>
	
		<h3>Twip 配置</h3>
		
		<form action="" method="post">
		
			<ul class="clearfix">
				<li><a href="oauth.php?type=1">OAuth 验证</a></li>
				<li><a class="active" href="oauth.php?type=2">模拟 OAuth 验证</a></li>
			</ul>
			
			<hr class="clear" />
			
			<p>
				<label for="url_suffix">1. 自定义 URL 地址</label>
                <input class="half" type="text" value="<?php echo BASE_URL.'o/';?>" id="base_url" disabled autocomplete="off" />
				<input class="half" type="text" value="" id="url_suffix" name="url_suffix" autocomplete="off" />
			</p>
			
			<p>
				<label for="username">2. 你的 Twitter 用户名</label>
				<input type="text" value="" id="username" name="username" autocomplete="off" />
			</p>
			
			<p>
				<label for="password">3. 你的 Twitter 密码</label>
				<input type="password" value="" id="password" name="password" autocomplete="off" />
			</p>
			
			<input type="submit" value="提交认证" class="button" />
		
		</form>
		
	</div>		
	<div id="footer">
		2012 &copy; <a href="http://code.google.com/p/twip/">Twip Project</a>
	</div>
</body>
</html>
<?php
}
?>
