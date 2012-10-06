<?php
	
	function encrypt($plain_text,$key) {	  
		try{
			$td = mcrypt_module_open('blowfish', '', 'cfb', '');
			$iv = mcrypt_create_iv(mcrypt_enc_get_iv_size($td), MCRYPT_RAND);
			mcrypt_generic_init($td, $key, $iv);
			$crypt_text = mcrypt_generic($td, $plain_text);
			mcrypt_generic_deinit($td);
			return base64_encode($iv.$crypt_text);
		} catch (Exception $e) {
			return '';
		}
	}
	  
	function decrypt($crypt_text,$key) {
		try{
			$crypt_text = base64_decode($crypt_text);
			$td = mcrypt_module_open('blowfish', '', 'cfb', '');
			$ivsize = mcrypt_enc_get_iv_size($td);
			$iv = substr($crypt_text, 0, $ivsize);
			$crypt_text = substr($crypt_text, $ivsize);
			mcrypt_generic_init($td, $key, $iv);
			$plain_text = mdecrypt_generic($td, $crypt_text);
			mcrypt_generic_deinit($td);
			return $plain_text;
		} catch (Exception $e) {
			return '';
		}
	}
?>