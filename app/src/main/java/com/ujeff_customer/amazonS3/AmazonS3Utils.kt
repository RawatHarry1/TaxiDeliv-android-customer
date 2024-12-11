package com.ujeff_customer.amazonS3


import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client


object AmazonS3Utils {
    private var mS3Client: AmazonS3Client? = null
    private var mCredProvider: CognitoCachingCredentialsProvider? = null
    private var mTransferUtility: TransferUtility? = null

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    fun getTransferUtility(context: Context): TransferUtility {

        if (mTransferUtility == null) {
            mTransferUtility =
                TransferUtility.builder().s3Client(getS3Client(context.applicationContext))
                    .context(context).build()
        }

        return mTransferUtility as TransferUtility
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    private fun getS3Client(context: Context): AmazonS3Client {
        if (mS3Client == null) {
            var config = ClientConfiguration().withConnectionTimeout(20000).withSocketTimeout(20000)
            mS3Client?.setConfiguration(config)
            mS3Client = AmazonS3Client(getCredProvider(context.applicationContext))

            // mS3Client!!.endpoint = AmazonS3Constants.END_POINT
        }
        return mS3Client as AmazonS3Client
    }


    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */
    private fun getCredProvider(context: Context): CognitoCachingCredentialsProvider? {
        if (mCredProvider == null) {
            mCredProvider = CognitoCachingCredentialsProvider(
                context, "BuildConfig.AMAZON_POOL_ID", // Identity Pool ID
                AmazonS3Constants.REGIONS // Region
            )
        }
        return mCredProvider
    }

    /**
     * Converts number of bytes into proper scale.
     *
     * @param bytes number of bytes to be converted.
     * @return A string that represents the bytes in a proper scale.
     */
    fun getBytesString(bytes: Long): String {
        val quantifiers = arrayOf("KB", "MB", "GB", "TB")
        var speedNum = bytes.toDouble()
        var i = 0
        while (true) {
            if (i >= quantifiers.size) {
                return ""
            }
            speedNum /= 1024.0
            if (speedNum < 512) {
                return String.format("%.2f", speedNum) + " " + quantifiers[i]
            }
            i++
        }
    }


}
