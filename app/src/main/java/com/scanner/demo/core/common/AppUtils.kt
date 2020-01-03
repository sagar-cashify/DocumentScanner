package com.scanner.demo.core.common

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class AppUtils {

    companion object{

        fun hasPermissions(context : AppCompatActivity, permissions : Array<String> ): Boolean{
            for(permission in permissions){
                when (ActivityCompat.checkSelfPermission(context , permission)){
                    PackageManager.PERMISSION_DENIED -> return false

                }

            }

            return true;
        }


        fun hasPermission(context : AppCompatActivity , permission : String ): Boolean{
               if (ActivityCompat.checkSelfPermission(context , permission) == PackageManager.PERMISSION_DENIED ){

               return false
                }



            return true;
        }



        fun reqPermission(context : AppCompatActivity , permissions : Array<String> , requestCode : Int){
            ActivityCompat.requestPermissions(context , permissions , requestCode)
        }
    }
}