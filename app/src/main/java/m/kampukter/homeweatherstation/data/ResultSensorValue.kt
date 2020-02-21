package m.kampukter.homeweatherstation.data

sealed class ResultSensorValue {
    data class Success(val sensorValue: List<SensorValue>) : ResultSensorValue()
    object EmptyResponse: ResultSensorValue()
    data class OtherError( val tError: String ) : ResultSensorValue()
}