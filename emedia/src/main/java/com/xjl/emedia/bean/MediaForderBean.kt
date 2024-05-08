package com.xjl.emedia.bean

import java.io.File

class MediaForderBean(var folderPath: String) {
    
    var folderName: String

    var num = 0
    
    var coverFilePath: String? = null

    init {
        folderName = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1)
    }
}
