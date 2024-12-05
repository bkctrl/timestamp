package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import android.content.pm.PackageManager
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import org.timestamp.mobile.R
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.sin
import androidx.compose.runtime.SideEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.util.Log
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.toOffset
import org.timestamp.mobile.ui.theme.Colors
import java.net.URL
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.TextField
import androidx.compose.material.Text
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.TextStyle
import androidx.wear.compose.material.ContentAlpha

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission() {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    SideEffect {
        permissionState.launchPermissionRequest()
    }
}

@SuppressLint("MissingPermission")
fun fetchCurrentLocation(
    context: Context,
    onLocationRetrieved: (LatLng) -> Unit
) {
    val permissionCheck = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                onLocationRetrieved(LatLng(location.latitude, location.longitude))
            } else {
                onLocationRetrieved(LatLng(37.7749, -122.4194)) // default, San Fran
            }
        }.addOnFailureListener {
            onLocationRetrieved(LatLng(37.7749, -122.4194))
        }
    } else {
        onLocationRetrieved(LatLng(37.7749, -122.4194))
    }
}

@Composable
fun FetchLocationWrapper(
    context: Context,
    onLocationRetrieved: (LatLng) -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    RequestLocationPermission()
    val permissionCheck = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    permissionGranted = permissionCheck == PackageManager.PERMISSION_GRANTED
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            fetchCurrentLocation(context, onLocationRetrieved)
        }
    }
}

fun fetchLocationDetails(
    context: Context,
    latLng: LatLng,
    onResult: (String, String) -> Unit,
    onError: (Exception) -> Unit
    ) {
    val apiKey = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData
        .getString("com.google.android.geo.API_KEY")
    val geocodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng=${latLng.latitude},${latLng.longitude}&key=$apiKey"
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = URL(geocodeUrl).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            if (results.length() > 0) {
                val result = results.getJSONObject(0)
                val addressComponents = result.getJSONArray("address_components")
                var buildingName: String? = null
                for (i in 0 until addressComponents.length()) {
                    val component = addressComponents.getJSONObject(i)
                    val types = component.getJSONArray("types")
                    for (j in 0 until types.length()) {
                        val type = types.getString(j)
                        if (type == "point_of_interest" || type == "premise") {
                            buildingName = component.getString("short_name")
                            break
                        }
                    }
                    if (buildingName != null) break
                }
                val formattedAddress = result.getString("formatted_address")
                val nameToUse = buildingName ?: formattedAddress
                withContext(Dispatchers.Main) {
                    onResult(nameToUse, formattedAddress)
                }
            } else {
                throw Exception("No results found.")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent(
    onDismissRequest: () -> Unit,
    onConfirmation: (EventDTO) -> Unit,
    isMock: Boolean,
    properties: DialogProperties = DialogProperties(),
    editEvent: EventDTO?,
    currentUser: FirebaseUser?
) {
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(false) }
    var eventTime by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var isLoadingLocation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    var isSearchActive by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            editEvent?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(37.7749, -122.4194),
            if (editEvent != null) 15f else 10f
        )
    }

    if (editEvent != null) {
        if (currentUser?.uid == editEvent.creator) {
            LaunchedEffect(editEvent) {
                eventName = editEvent.name
                val date = Date.from(editEvent.arrival.toInstant())
                selectedDate = dateFormatter.format(date)
                selectedTime = timeFormatter.format(date)
                selectedLocation = LatLng(editEvent.latitude, editEvent.longitude)
                fetchLocationDetails(
                    context = context,
                    latLng = LatLng(editEvent.latitude, editEvent.longitude),
                    onResult = { name, address ->
                        locationName = name
                        locationAddress = address
                    },
                    onError = { error ->
                        // Handle any errors during location detail retrieval
                        locationName = "Unknown Location"
                        locationAddress = "Failed to fetch address"
                        error.printStackTrace()
                    }
                )
                cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLocation!!, 15f)
                query = locationName
            }
        } else {
            eventName = editEvent.name
            val date = Date.from(editEvent.arrival.toInstant())
            selectedDate = dateFormatter.format(date)
            selectedTime = timeFormatter.format(date)
            selectedLocation = LatLng(editEvent.latitude, editEvent.longitude)
            fetchLocationDetails(
                context = context,
                latLng = LatLng(editEvent.latitude, editEvent.longitude),
                onResult = { name, address ->
                    locationName = name
                    locationAddress = address
                },
                onError = { error ->
                    // Handle any errors during location detail retrieval
                    locationName = "Unknown Location"
                    locationAddress = "Failed to fetch address"
                    error.printStackTrace()
                }
            )
            cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLocation!!, 15f)
            query = locationName
        }
    }
    LaunchedEffect(locationName) {
        if (!isSearchActive) {
            query = locationName
        }
    }
    val defaultMockLocation = LatLng(37.7749, -122.4194)
    if (isMock) {
        // Use mock location
        selectedLocation = defaultMockLocation
        cameraPositionState.position = CameraPosition.fromLatLngZoom(defaultMockLocation, 15f)
    } else {
        // Real location fetching logic
        FetchLocationWrapper(context) { location ->
            selectedLocation = location
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
            isLoadingLocation = true
            fetchLocationDetails(
                context = context,
                latLng = location,
                onResult = { name, address ->
                    locationName = name
                    locationAddress = address
                    isLoadingLocation = false
                    isSearchActive = false
                    query = locationName
                },
                onError = { error ->
                    Log.e("CreateEvent", "Error fetching location: ${error.message}")
                    locationName = "Failed to fetch location"
                    locationAddress = ""
                    isLoadingLocation = false
                }
            )
        }
    }

    if (eventDate) {
        DatePickerDialog(
            onDateSelected = { date ->
                eventDate = false
                selectedDate = date},
            onDismiss = { eventDate = false },
            initialDate = selectedDate
        )
    }
    if (eventTime) {
        TimePickerDialog(
            onConfirm = { hour, minute ->
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                eventTime = false
            },
            onDismiss = { eventTime = false },
        )
    }

    LaunchedEffect(query) {
        if (isSearchActive && query.isNotEmpty()) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    predictions = response.autocompletePredictions
                }
                .addOnFailureListener {
                    predictions = emptyList()
                }
        } else {
            predictions = emptyList()
        }
    }

   Dialog(
       onDismissRequest = { onDismissRequest() },
       properties = properties.let {
           DialogProperties(
               usePlatformDefaultWidth = false
           )
       }) {
       Card (
           modifier = Modifier
               .fillMaxWidth(0.92f)
               .height(640.dp)
               .shadow(6.dp, shape = RoundedCornerShape(32.dp))
               .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(32.dp)),
           shape = RoundedCornerShape(32.dp)
       ) {
           Column(
               modifier = Modifier
                   .background(MaterialTheme.colors.primary)
                   .fillMaxSize(),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
           ) {
               Text(
                   modifier = Modifier
                       .padding(16.dp),
                   text = if (currentUser?.uid != editEvent?.creator && editEvent != null) "View Event" else if (editEvent == null) "Add Event" else "Edit Event",
                   color = MaterialTheme.colors.secondary,
                   fontFamily = ubuntuFontFamily,
                   fontWeight = FontWeight.Bold,
                   fontSize = 24.sp
               )
               Divider(
                   color = Color.LightGray,
                   thickness = 2.dp,
                   modifier = Modifier
                       .fillMaxWidth(0.9f)
                       .padding(bottom = 16.dp)
               )
               TextField(
                   modifier = Modifier
                       .fillMaxWidth(0.9f)
                       .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x33000000))
                       .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                       .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(16.dp)),
                   value = eventName,
                   onValueChange = {eventName = it},
                   placeholder = { Text(
                       text = "Event Name",
                       fontFamily = ubuntuFontFamily,
                       fontWeight = FontWeight.Bold) },
                   singleLine = true,
                   enabled = if (currentUser?.uid == editEvent?.creator || editEvent == null) true else false,
                   colors = TextFieldDefaults.textFieldColors(
                       backgroundColor = Color.Transparent,
                       textColor = MaterialTheme.colors.secondary
                   ),
               )
               Spacer(modifier = Modifier.height(12.dp))
               Row(
                   modifier = Modifier
                       .fillMaxWidth(),
                   horizontalArrangement = Arrangement.Center,
               ) {
                   TextField(
                       modifier = Modifier
                           .fillMaxWidth(0.45f)
                           .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x33000000))
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(16.dp)),
                       value = selectedDate,
                       onValueChange = { },
                       readOnly = true,
                       placeholder = {
                           Text(
                               text = "Event Date",
                               fontFamily = ubuntuFontFamily,
                               fontWeight = FontWeight.Bold
                           )
                       },
                       trailingIcon = {
                           IconButton(onClick = {
                               if (currentUser?.uid == editEvent?.creator || editEvent == null) {
                                   eventDate = !eventDate
                               }
                           }) {
                               Icon(
                                   imageVector = Icons.Default.DateRange,
                                   contentDescription = "select date"
                               )
                           }
                       },
                       colors = TextFieldDefaults.textFieldColors(
                           backgroundColor = Color.Transparent,
                           textColor = MaterialTheme.colors.secondary
                       )
                   )
                   Spacer(modifier = Modifier.width(8.dp))
                   TextField(
                       modifier = Modifier
                           .fillMaxWidth(0.8f)
                           .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x33000000))
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(16.dp))
                           .clickable {
                               if (currentUser?.uid == editEvent?.creator || editEvent == null) {
                                   eventTime = !eventTime
                               }
                                      },
                       enabled = false,
                       value = selectedTime,
                       onValueChange = {selectedTime = it},
                       placeholder = { Text(
                           text = "Event Time",
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold) },
                       singleLine = true,
                       readOnly = true,
                       colors = TextFieldDefaults.textFieldColors(
                           backgroundColor = Color.Transparent,
                           textColor = MaterialTheme.colors.secondary,
                       )
                   )
               }
               Spacer(modifier = Modifier.height(12.dp))
               if (isSearchActive) {
                   TextField(
                       value = query,
                       onValueChange = { text -> query = text },
                       placeholder = {
                           Text(
                               text = "Type to search...",
                               fontFamily = ubuntuFontFamily,
                               fontWeight = FontWeight.Bold,
                               fontSize = 16.sp,
                               color = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium)
                           )
                       },
                       textStyle = TextStyle(
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold,
                           fontSize = 16.sp,
                           color = MaterialTheme.colors.secondary
                       ),
                       singleLine = true,
                       modifier = Modifier
                           .fillMaxWidth(0.9f)
                           .height(56.dp)
                           .shadow(
                               4.dp,
                               shape = RoundedCornerShape(16.dp),
                               ambientColor = Color(0x33000000)
                           )
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(
                               MaterialTheme.colors.primary,
                               shape = RoundedCornerShape(16.dp)
                           ),
                       colors = TextFieldDefaults.textFieldColors(
                           backgroundColor = Color.Transparent,
                           textColor = MaterialTheme.colors.secondary,
                           cursorColor = MaterialTheme.colors.secondary,
                           focusedIndicatorColor = Color.Transparent,
                           unfocusedIndicatorColor = Color.Transparent,
                           placeholderColor = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium)
                       ),
                       enabled = currentUser?.uid == editEvent?.creator || editEvent == null
                   )
                   if (predictions.isNotEmpty()) {
                       LazyColumn(
                           modifier = Modifier
                               .fillMaxWidth(0.9f)
                               .heightIn(max = 200.dp)
                       ) {
                           items(predictions) { prediction ->
                               Text(
                                   text = prediction.getFullText(null).toString(),
                                   modifier = Modifier
                                       .fillMaxWidth()
                                       .clickable {
                                           val placeFields = listOf(
                                               Place.Field.ID,
                                               Place.Field.NAME,
                                               Place.Field.ADDRESS,
                                               Place.Field.LAT_LNG
                                           )
                                           val request = FetchPlaceRequest.builder(
                                               prediction.placeId,
                                               placeFields
                                           ).build()
                                           placesClient.fetchPlace(request)
                                               .addOnSuccessListener { response ->
                                                   val place = response.place
                                                   selectedLocation = place.latLng
                                                   locationName = place.name ?: ""
                                                   locationAddress = place.address ?: ""
                                                   cameraPositionState.position =
                                                       CameraPosition.fromLatLngZoom(
                                                           place.latLng!!,
                                                           15f
                                                       )
                                                   isSearchActive = false
                                                   query = locationName
                                               }
                                               .addOnFailureListener { exception ->
                                                   Log.e(
                                                       "Places API",
                                                       "Place not found: ${exception.message}"
                                                   )
                                               }
                                       }
                                       .padding(16.dp)
                               )
                           }
                       }
                   }
               } else {
                   Box(
                       modifier = Modifier
                           .fillMaxWidth(0.9f)
                           .height(56.dp)
                           .shadow(
                               4.dp,
                               shape = RoundedCornerShape(16.dp),
                               ambientColor = Color(0x33000000)
                           )
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(
                               MaterialTheme.colors.primary,
                               shape = RoundedCornerShape(16.dp)
                           )
                           .clickable {
                               isSearchActive = true
                           },
                       contentAlignment = Alignment.CenterStart
                   ) {
                       Text(
                           text = if (locationName.isNotEmpty()) locationName else "Event Location",
                           modifier = Modifier
                               .fillMaxWidth()
                               .padding(horizontal = 16.dp, vertical = 12.dp),
                           color = if (locationName.isNotEmpty()) MaterialTheme.colors.secondary else MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium),
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold,
                           fontSize = 16.sp
                       )
                   }
               }
               Spacer(modifier = Modifier.height(16.dp))
                   GoogleMap(
                       modifier = Modifier
                           .fillMaxWidth(0.95f)
                           .height(270.dp)
                           .clip(RoundedCornerShape(16.dp)),
                       cameraPositionState = cameraPositionState,
                       onMapClick = { latLng ->
                           selectedLocation = latLng
                           isLoadingLocation = true
                           if (currentUser?.uid == editEvent?.creator || editEvent == null) {
                               fetchLocationDetails(
                                   context = context,
                                   latLng = latLng,
                                   onResult = { name, address ->
                                       locationName = name
                                       locationAddress = address
                                       isLoadingLocation = false
                                       isSearchActive = false
                                       query = locationName
                                   },
                                   onError = { error ->
                                       Log.e("CreateEvent", "Error fetching location: ${error.message}")
                                       locationName = "Failed to fetch location"
                                       locationAddress = ""
                                       isLoadingLocation = false
                                   }
                               )
                           }
                       }
                   ) {
                       selectedLocation?.let {
                           Marker(
                               state = MarkerState(position = it),
                               title = "Selected Location"
                           )
                       }
                   }
               Spacer(modifier = Modifier.height(12.dp))
               Row(
                   modifier = Modifier
                       .fillMaxWidth(),
                   horizontalArrangement = Arrangement.Center,
               ) {
                   Button(
                       onClick = { onDismissRequest() },
                       colors = ButtonColors(
                           containerColor = Color(0xFF2A2B2E),
                           contentColor = Color(0xFFFFFFFF),
                           disabledContainerColor = Color(0xFF2A2B2E),
                           disabledContentColor = Color(0xFFFFFFFF)
                       ),
                       shape = RoundedCornerShape(24.dp),
                       modifier = Modifier
                           .width(130.dp)
                           .height(50.dp)
                           .shadow(4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0x33000000))
                   ) {
                       Text(
                           text = "Cancel",
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold,
                           fontSize = 24.sp,
                           color = Color(0xFFFFFFFF)
                       )
                   }
                   if (currentUser?.uid == editEvent?.creator || editEvent == null) {
                       Spacer(modifier = Modifier.width(32.dp));
                       Button(
                           onClick = {
                               if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                                   val date = dateFormatter.parse(selectedDate)
                                   val parsedTime = timeFormatter.parse(selectedTime)

                                   if (date != null && parsedTime != null) {
                                       val calendar = Calendar.getInstance().apply {
                                           time = date
                                       }
                                       val timeCalendar = Calendar.getInstance().apply {
                                           time = parsedTime
                                       }

                                       val selectedDateTime = LocalDateTime.of(
                                           calendar.get(Calendar.YEAR),
                                           calendar.get(Calendar.MONTH) + 1, // Months are zero-based
                                           calendar.get(Calendar.DAY_OF_MONTH),
                                           timeCalendar.get(Calendar.HOUR_OF_DAY),
                                           timeCalendar.get(Calendar.MINUTE)
                                       )
                                       val currentDateTime = LocalDateTime.now()
                                       Log.d("Time", selectedDateTime.toOffset().toString())
                                       if (selectedDateTime.isAfter(currentDateTime)) {
                                           if (editEvent != null) {
                                               onConfirmation(
                                                   EventDTO(
                                                       name = eventName,
                                                       arrival = selectedDateTime.toOffset(),
                                                       latitude = selectedLocation?.latitude ?: 0.0,
                                                       longitude = selectedLocation?.longitude ?: 0.0,
                                                       description = locationName,
                                                       address = locationAddress,
                                                       id = editEvent.id,
                                                   )
                                               )
                                           } else {
                                               onConfirmation(
                                                   EventDTO(
                                                       name = eventName,
                                                       arrival = selectedDateTime.toOffset(),
                                                       latitude = selectedLocation?.latitude ?: 0.0,
                                                       longitude = selectedLocation?.longitude ?: 0.0,
                                                       description = locationName,
                                                       address = locationAddress,
                                                   )
                                               )
                                           }
                                           Log.d("ADD EVENT", "EVENT ADDED")
                                           Log.d("selectedDate", selectedDate)
                                       }
                                   }
                               } else {
                                   Log.d("ADD EVENT", "EVENT FAILED TO ADD")
                                   Log.d("selectedDate", selectedDate)
                               }
                           },
                           colors = ButtonColors(
                               containerColor = Color(0xFFFF6F61),
                               contentColor = Color(0xFFFFFFFF),
                               disabledContainerColor = Color(0xFFFF6F61),
                               disabledContentColor = Color(0xFFFFFFFF)
                           ),
                           shape = RoundedCornerShape(24.dp),
                           modifier = Modifier
                               .width(130.dp)
                               .height(50.dp)
                               .shadow(4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0x33000000))
                       ) {
                           Text(
                               text = if (editEvent == null) "Add" else "Edit",
                               fontFamily = ubuntuFontFamily,
                               fontWeight = FontWeight.Bold,
                               fontSize = 24.sp,
                               color = Color(0xFFFFFFFF)
                           )
                       }
                   }
               }
           }
       }
   }
}
