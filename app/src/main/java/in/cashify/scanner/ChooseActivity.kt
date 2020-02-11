package `in`.cashify.scanner

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import `in`.cashify.scanner.common.AppConstant
import `in`.cashify.scanner.common.AppUtils
import `in`.cashify.scanner.ui.CameraActivity


class ChooseActivity : AppCompatActivity() {
    lateinit var btn_file: Button
    lateinit var btn_cam: Button

    var cameraPermission = android.Manifest.permission.CAMERA
    var writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    var readPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE

    var REQUEST_CAMERA_PERMISSION = 101
    var REQUEST_GALLERY_PERMISSION = 102


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
        btn_cam = findViewById(R.id.btn_cam)
        btn_file = findViewById(R.id.btn_file)

        btn_file.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, readPermission) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, writePermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@ChooseActivity, arrayOf(readPermission, writePermission), REQUEST_GALLERY_PERMISSION)

            } else {
                AppUtils.openGallery(this@ChooseActivity)
            }

        }


        btn_cam.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, writePermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@ChooseActivity, arrayOf(cameraPermission, writePermission), REQUEST_CAMERA_PERMISSION)

            } else {
                startActivityForResult(Intent(this@ChooseActivity, CameraActivity::class.java), AppConstant.REQUEST_CODE)

            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_GALLERY_PERMISSION -> {
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this@ChooseActivity, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show()
                        break;

                    }
                }

                AppUtils.openGallery(this@ChooseActivity)
            }

            REQUEST_CAMERA_PERMISSION -> {
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this@ChooseActivity, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show()
                        break;

                    }
                }

                startActivityForResult(Intent(this@ChooseActivity, CameraActivity::class.java), AppConstant.REQUEST_CODE)

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstant.OPEN_GALLERY_RESULT) {
            if(resultCode == RESULT_OK ){
                val intent = Intent(this@ChooseActivity, ScanActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable(ScanConstants.SELECTED_BITMAP, data?.data)
                intent.putExtra(ScanConstants.IS_GALLERY , true)
                intent.putExtras(bundle)
                startActivityForResult(intent, AppConstant.REQUEST_CODE)



            }
            else{
                Toast.makeText(this, "Some Error Occur", Toast.LENGTH_LONG).show()
            }
        }


        if (requestCode == AppConstant.REQUEST_CODE ) {
            if(resultCode == RESULT_OK ) {

                val uri = data?.getExtras()!!.getParcelable<Uri>(ScanConstants.SCANNED_RESULT)
                val intent = Intent()
                intent.putExtra(ScanConstants.SCANNED_RESULT, uri)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            else{
                Toast.makeText(this, "Some Error Occur", Toast.LENGTH_LONG).show()
            }

        }





    }






}
