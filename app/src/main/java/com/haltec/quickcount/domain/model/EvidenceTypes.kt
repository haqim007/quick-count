package com.haltec.quickcount.domain.model

//Should be match with R.string.photo_1 and so on.
enum class EvidenceTypes(val text: String, val value: String) {
    PHOTO_1("Foto 1", "Bukti 1"),
    PHOTO_2("Foto 2", "Bukti 2"),
    PHOTO_3("Foto 3", "Bukti 3"),
    PHOTO_4("Foto 4", "Bukti 4"),
    PHOTO_5("Foto 5", "Bukti 5"),
    PHOTO_6("Foto 6", "Bukti 6"),
    PHOTO_7("Foto 7", "Bukti 7"),
    OTHER("Lainnya", "Lainnya")
}

fun stringToEvidenceType(text: String) : EvidenceTypes{
    return when(text){
        EvidenceTypes.PHOTO_1.text -> EvidenceTypes.PHOTO_1
        EvidenceTypes.PHOTO_2.text -> EvidenceTypes.PHOTO_2
        EvidenceTypes.PHOTO_3.text -> EvidenceTypes.PHOTO_3
        EvidenceTypes.PHOTO_4.text -> EvidenceTypes.PHOTO_4
        EvidenceTypes.PHOTO_5.text -> EvidenceTypes.PHOTO_5
        EvidenceTypes.PHOTO_6.text -> EvidenceTypes.PHOTO_6
        EvidenceTypes.PHOTO_7.text -> EvidenceTypes.PHOTO_7
        else -> EvidenceTypes.OTHER
    }
}

fun valueToEvidenceTypeText(value: String) : String{
    return when(value){
        EvidenceTypes.PHOTO_1.value -> EvidenceTypes.PHOTO_1.text
        EvidenceTypes.PHOTO_2.value -> EvidenceTypes.PHOTO_2.text
        EvidenceTypes.PHOTO_3.value -> EvidenceTypes.PHOTO_3.text
        EvidenceTypes.PHOTO_4.value -> EvidenceTypes.PHOTO_4.text
        EvidenceTypes.PHOTO_5.value -> EvidenceTypes.PHOTO_5.text
        EvidenceTypes.PHOTO_6.value -> EvidenceTypes.PHOTO_6.text
        EvidenceTypes.PHOTO_7.value -> EvidenceTypes.PHOTO_7.text
        else -> EvidenceTypes.OTHER.text
    }
}

