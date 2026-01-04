package com.example.attendancetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

// ===== DATA MODELS =====

data class Student(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val rollNumber: Int
)

data class AttendanceRecord(
    val studentId: String,
    val studentName: String,
    val date: String,
    val className: String,
    val isPresent: Boolean
)

enum class Screen {
    CLASS_SELECTION,
    DATE_SELECTION,
    STUDENT_LIST,
    ATTENDANCE_MARKING
}

// ===== VIEW MODEL =====

class AttendanceViewModel : ViewModel() {
    // Current navigation state
    var currentScreen by mutableStateOf(Screen.CLASS_SELECTION)
    var selectedClass by mutableStateOf("")
    var selectedDate by mutableStateOf("")

    // Storage for students (in real app, this would be in database)
    private val studentsData = mapOf(
        "Class 1" to listOf(
            Student(name = "Rahul Kumar", rollNumber = 1),
            Student(name = "Priya Singh", rollNumber = 2),
            Student(name = "Amit Sharma", rollNumber = 3),
            Student(name = "Sneha Patel", rollNumber = 4),
            Student(name = "Vijay Verma", rollNumber = 5),
        ),
        "Class 2" to listOf(
            Student(name = "Ananya Das", rollNumber = 1),
            Student(name = "Rohan Gupta", rollNumber = 2),
            Student(name = "Kavya Reddy", rollNumber = 3),
            Student(name = "Arjun Nair", rollNumber = 4),
            Student(name = "Meera Shah", rollNumber = 5),
        ),
        "Class 3" to listOf(
            Student(name = "Karan Mehta", rollNumber = 1),
            Student(name = "Diya Joshi", rollNumber = 2),
            Student(name = "Aditya Iyer", rollNumber = 3),
            Student(name = "Riya Desai", rollNumber = 4),
            Student(name = "Siddharth Rao", rollNumber = 5),
        ),
        "Class 4" to listOf(
            Student(name = "Ishaan Kapoor", rollNumber = 1),
            Student(name = "Aarav Malhotra", rollNumber = 2),
            Student(name = "Tanvi Agarwal", rollNumber = 3),
            Student(name = "Neha Bansal", rollNumber = 4),
            Student(name = "Kunal Saxena", rollNumber = 5),
        ),
        "Class 5" to listOf(
            Student(name = "Pooja Chawla", rollNumber = 1),
            Student(name = "Vikram Bhatia", rollNumber = 2),
            Student(name = "Simran Khanna", rollNumber = 3),
            Student(name = "Nikhil Sinha", rollNumber = 4),
            Student(name = "Anjali Mishra", rollNumber = 5),
        )
    )

    // Attendance tracking (studentId -> isPresent)
    private val _attendanceMap = mutableStateMapOf<String, Boolean>()
    val attendanceMap: Map<String, Boolean> = _attendanceMap

    // Saved records (in real app, use Room database)
    private val _savedRecords = mutableStateListOf<AttendanceRecord>()
    val savedRecords: List<AttendanceRecord> = _savedRecords

    fun getStudentsForClass(className: String): List<Student> {
        return studentsData[className] ?: emptyList()
    }

    fun selectClass(className: String) {
        selectedClass = className
        currentScreen = Screen.DATE_SELECTION
    }

    fun selectDate(date: String) {
        selectedDate = date
        currentScreen = Screen.STUDENT_LIST

        // Initialize attendance map with all students as present by default
        val students = getStudentsForClass(selectedClass)
        _attendanceMap.clear()
        students.forEach { student ->
            _attendanceMap[student.id] = true // Default: all present
        }
    }

    fun toggleAttendance(studentId: String) {
        _attendanceMap[studentId] = !(_attendanceMap[studentId] ?: true)
    }

    fun saveAttendance() {
        val students = getStudentsForClass(selectedClass)
        students.forEach { student ->
            val record = AttendanceRecord(
                studentId = student.id,
                studentName = student.name,
                date = selectedDate,
                className = selectedClass,
                isPresent = _attendanceMap[student.id] ?: true
            )
            _savedRecords.add(record)
        }

        // Reset and go back to home
        goToHome()
    }

    fun goBack() {
        currentScreen = when (currentScreen) {
            Screen.DATE_SELECTION -> Screen.CLASS_SELECTION
            Screen.STUDENT_LIST -> Screen.DATE_SELECTION
            Screen.ATTENDANCE_MARKING -> Screen.STUDENT_LIST
            else -> Screen.CLASS_SELECTION
        }
    }

    fun goToHome() {
        currentScreen = Screen.CLASS_SELECTION
        selectedClass = ""
        selectedDate = ""
        _attendanceMap.clear()
    }
}

// ===== MAIN ACTIVITY =====

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AttendanceApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceApp(viewModel: AttendanceViewModel = viewModel()) {
    Scaffold(
        topBar = {
            if (viewModel.currentScreen != Screen.CLASS_SELECTION) {
                TopAppBar(
                    title = {
                        Text(
                            when (viewModel.currentScreen) {
                                Screen.DATE_SELECTION -> "Select Date"
                                Screen.STUDENT_LIST -> "${viewModel.selectedClass} - ${viewModel.selectedDate}"
                                else -> "Attendance"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6200EE),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color(0xFFF5F5F5)
        ) {
            when (viewModel.currentScreen) {
                Screen.CLASS_SELECTION -> ClassSelectionScreen(viewModel)
                Screen.DATE_SELECTION -> DateSelectionScreen(viewModel)
                Screen.STUDENT_LIST -> StudentListScreen(viewModel)
                else -> ClassSelectionScreen(viewModel)
            }
        }
    }
}

// ===== SCREEN 1: CLASS SELECTION =====

@Composable
fun ClassSelectionScreen(viewModel: AttendanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Attendance Tracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE)
        )

        Text(
            text = "Select a class to mark attendance",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Class buttons
        val classes = listOf("Class 1", "Class 2", "Class 3", "Class 4", "Class 5")

        classes.forEach { className ->
            ClassButton(
                className = className,
                onClick = { viewModel.selectClass(className) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ClassButton(className: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = className,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6200EE)
            )
        }
    }
}

// ===== SCREEN 2: DATE SELECTION =====

@Composable
fun DateSelectionScreen(viewModel: AttendanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Select Date",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Today button
        DateOptionButton(
            label = "Today",
            date = getTodayDate(),
            onClick = { viewModel.selectDate(getTodayDate()) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Yesterday button
        DateOptionButton(
            label = "Yesterday",
            date = getYesterdayDate(),
            onClick = { viewModel.selectDate(getYesterdayDate()) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Or select custom date",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick date buttons for last 7 days
        LazyColumn {
            items(7) { index ->
                val date = getDateDaysAgo(index)
                DateOptionButton(
                    label = if (index == 0) "Today" else "$index days ago",
                    date = date,
                    onClick = { viewModel.selectDate(date) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun DateOptionButton(label: String, date: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(text = date, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

// ===== SCREEN 3: STUDENT LIST WITH ATTENDANCE =====

@Composable
fun StudentListScreen(viewModel: AttendanceViewModel) {
    val students = viewModel.getStudentsForClass(viewModel.selectedClass)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Student list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(students) { student ->
                StudentAttendanceItem(
                    student = student,
                    isPresent = viewModel.attendanceMap[student.id] ?: true,
                    onToggle = { viewModel.toggleAttendance(student.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Save button
        Button(
            onClick = { viewModel.saveAttendance() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Text("Save Attendance", fontSize = 18.sp)
        }
    }
}

@Composable
fun StudentAttendanceItem(
    student: Student,
    isPresent: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPresent) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Roll number badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isPresent) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.rollNumber.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Student name
                Text(
                    text = student.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Status icon
            Icon(
                imageVector = if (isPresent) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isPresent) "Present" else "Absent",
                tint = if (isPresent) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ===== UTILITY FUNCTIONS =====

fun getTodayDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun getYesterdayDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(calendar.time)
}

fun getDateDaysAgo(daysAgo: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(calendar.time)
}