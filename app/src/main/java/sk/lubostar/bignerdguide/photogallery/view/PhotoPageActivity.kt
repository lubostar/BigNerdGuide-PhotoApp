package sk.lubostar.bignerdguide.photogallery.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import sk.lubostar.bignerdguide.photogallery.R

class PhotoPageActivity: AppCompatActivity() {
    companion object {
        fun newIntent(context: Context, photoPageUri: Uri): Intent {
            return Intent(context, PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_page)

        with(supportFragmentManager){
            val currentFragment = findFragmentById(R.id.fragment_container)

            if (currentFragment == null) {
                val fragment = PhotoPageFragment.newInstance(intent.data!!)
                beginTransaction().add(R.id.fragment_container, fragment).commit()
            }
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null && currentFragment is PhotoPageFragment) {
            currentFragment.onBackPressed {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}