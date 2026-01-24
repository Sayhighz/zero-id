class ProofViewModel : ViewModel() {
    // สร้าง State สำหรับบอก UI ว่ากำลังโหลดหรือสำเร็จ
    var isVerifying by mutableStateOf(false)
    var verificationResult by mutableStateOf<VerifyResponse?>(null)

    fun sendVerification(proofFromWebView: Any, signals: List<String>) {
        isVerifying = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = VerifyRequest(proof = proofFromWebView, publicSignals = signals)
                val response = ApiClient.instance.verifyProof(request)
                
                withContext(Dispatchers.Main) {
                    isVerifying = false
                    if (response.isSuccessful) {
                        verificationResult = response.body()
                        // ส่ง Log เช็คผล
                        Log.d("ZeroID", "Success: ${verificationResult?.message}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isVerifying = false
                    Log.e("ZeroID", "Network Error: ${e.message}")
                }
            }
        }
    }
}