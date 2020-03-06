package com.warning.fragment

import android.app.Fragment
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.warning.R
import com.warning.activity.WarningDetailActivity
import com.warning.common.CONST
import com.warning.dto.WarningDto
import com.warning.util.CommonUtil
import kotlinx.android.synthetic.main.fragment_warning.*

/**
 * 预警文档
 */
class WarningFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_warning, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
    }

    private fun initWidget() {
        val dto : WarningDto = arguments.getParcelable("data")
        if (!TextUtils.isEmpty(dto.name)) {
            tvTitle.text = dto.name
        }
        var bitmap: Bitmap? = null
        if (dto.color == CONST.blue[0]) {
            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + dto.type + CONST.blue[1] + CONST.imageSuffix)
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.blue[1] + CONST.imageSuffix)
            }
        } else if (dto.color == CONST.yellow[0]) {
            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + dto.type + CONST.yellow[1] + CONST.imageSuffix)
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.yellow[1] + CONST.imageSuffix)
            }
        } else if (dto.color == CONST.orange[0]) {
            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + dto.type + CONST.orange[1] + CONST.imageSuffix)
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.orange[1] + CONST.imageSuffix)
            }
        } else if (dto.color == CONST.red[0]) {
            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + dto.type + CONST.red[1] + CONST.imageSuffix)
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.red[1] + CONST.imageSuffix)
            }
        } else if (dto.color == CONST.unknown[0]) {
            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/default" + CONST.imageSuffix)
        }
        imageView.setImageBitmap(bitmap)

        llContent.setOnClickListener {
            val intent = Intent(activity, WarningDetailActivity::class.java)
            intent.putExtra("url", dto.html)
            startActivity(intent)
        }
    }

}
