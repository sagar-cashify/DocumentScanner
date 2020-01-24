package `in`.cashify.scanner.common

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.provider.MediaStore


class AppUtils {

    companion object{
        fun openGallery(activity : AppCompatActivity){
            var pickPhoto = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            activity.startActivityForResult(pickPhoto, AppConstant.OPEN_GALLERY_RESULT)
        }

    }
}