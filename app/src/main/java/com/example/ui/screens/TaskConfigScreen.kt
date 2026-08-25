package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutomationMode
import com.example.model.TaskItem
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleDark
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AutomatorViewModel

@Composable
fun TaskConfigScreen(
  viewModel: AutomatorViewModel,
  modifier: Modifier = Modifier
) {
  val taskList by viewModel.taskList.collectAsState()
  val currentTaskIndex by viewModel.currentTaskIndex.collectAsState()
  val isAutomating by viewModel.isAutomating.collectAsState()
  val currentConfig by viewModel.taskConfig.collectAsState()

  // State for Add / Edit Task Modal
  var editingTask by remember { mutableStateOf<TaskItem?>(null) }
  var isAddingNewTask by remember { mutableStateOf(false) }

  // State for Delete Confirmation Modal
  var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }

  // Global CPAGrip Settings Modal
  var showGlobalSettingsDialog by remember { mutableStateOf(false) }

  val clipboardManager = LocalClipboardManager.current

  val userAgents = listOf(
    "iPhone / Safari iOS 17.4 (Mobile)",
    "Android Chrome 124 (Mobile)",
    "Windows 11 / Chrome 124 (Desktop)",
    "Mac OS X / Safari 17 (Desktop)"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFEF7FF))
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header & Top Control Bar
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "قائمة المهام المتسلسلة (Task Queue)",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "تنفيذ المهام بالتتابع مع Referrer عشوائي و User-Agent لكل مهمة",
              fontSize = 11.sp,
              color = TextSecondary
            )
          }

          IconButton(
            onClick = { showGlobalSettingsDialog = true },
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(PrimaryPurpleLight.copy(alpha = 0.5f))
              .testTag("open_global_settings_btn")
          ) {
            Icon(
              imageVector = Icons.Outlined.Settings,
              contentDescription = "CPAGrip & Global Settings",
              tint = PrimaryPurpleDark,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Top Main Actions Bar: Start / Stop / Add Task
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!isAutomating) {
            Button(
              onClick = { viewModel.startAutomation() },
              modifier = Modifier
                .weight(1.3f)
                .height(48.dp)
                .testTag("start_queue_btn"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
              Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(6.dp))
              Text("تشغيل المهام", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          } else {
            Button(
              onClick = { viewModel.stopAutomation() },
              modifier = Modifier
                .weight(1.3f)
                .height(48.dp)
                .testTag("stop_queue_btn"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
              Icon(Icons.Filled.Stop, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(6.dp))
              Text("إيقاف التشغيل", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }

          Button(
            onClick = { isAddingNewTask = true },
            modifier = Modifier
              .weight(1.2f)
              .height(48.dp)
              .testTag("add_task_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("إضافة مهمة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }

    // 2. Queue Status Overview Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isAutomating) StatusGreen else StatusYellow)
            )
            Text(
              text = if (isAutomating) "قيد التشغيل المتسلسل (Queue Active)" else "جاهز للتشغيل (Standby)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (isAutomating) StatusGreen else TextPrimary
            )
          }

          Text(
            text = "إجمالي المهام: ${taskList.size} مهمة",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryPurpleDark
          )
        }
      }
    }

    // 3. Task List Section
    if (taskList.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceLight),
          border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Link,
              contentDescription = null,
              tint = TextSecondary,
              modifier = Modifier.size(40.dp)
            )
            Text(
              text = "لا توجد مهام في القائمة حالياً",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "انقر على زر 'إضافة مهمة' لإدراج أول مهمة CPA مع إعداداتها الخاصة",
              fontSize = 11.sp,
              color = TextSecondary
            )
            Button(
              onClick = { isAddingNewTask = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
              Icon(Icons.Filled.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(4.dp))
              Text("إضافة أول مهمة")
            }
          }
        }
      }
    } else {
      itemsIndexed(taskList, key = { _, task -> task.id }) { index, task ->
        TaskItemCard(
          task = task,
          taskIndex = index,
          totalTasks = taskList.size,
          isActive = isAutomating && currentTaskIndex == index,
          onEditClick = { editingTask = task },
          onDeleteClick = { taskToDelete = task },
          onDuplicateClick = { viewModel.duplicateTask(task.id) },
          onMoveUp = if (index > 0) { { viewModel.moveTaskUp(index) } } else null,
          onMoveDown = if (index < taskList.size - 1) { { viewModel.moveTaskDown(index) } } else null,
          userAgents = userAgents
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }

  // Dialog: Add New Task
  if (isAddingNewTask) {
    TaskEditDialog(
      title = "إضافة مهمة جديدة (Add New CPA Task)",
      initialTask = TaskItem(
        title = "مهمة #${taskList.size + 1}",
        offerUrl = "https://rileymarker.com/show.php?l=0&u=2227942&id=74924",
        referrerBaseUrl = "https://www.google.com/search",
        useRandomReferrer = true,
        selectedUserAgentIndex = 0,
        selectedMode = AutomationMode.MODE_3_SMART_COMPLETION,
        taskRepeatCount = 1,
        browserDurationSeconds = 55
      ),
      userAgents = userAgents,
      onDismiss = { isAddingNewTask = false },
      onSave = { newTask ->
        viewModel.addTask(newTask)
        isAddingNewTask = false
      }
    )
  }

  // Dialog: Edit Existing Task
  editingTask?.let { task ->
    TaskEditDialog(
      title = "تعديل المهمة #${taskList.indexOfFirst { it.id == task.id } + 1}",
      initialTask = task,
      userAgents = userAgents,
      onDismiss = { editingTask = null },
      onSave = { updated ->
        viewModel.updateTask(updated)
        editingTask = null
      }
    )
  }

  // Dialog: Delete Confirmation
  taskToDelete?.let { task ->
    AlertDialog(
      onDismissRequest = { taskToDelete = null },
      title = { Text("حذف المهمة", fontWeight = FontWeight.Bold) },
      text = { Text("هل أنت متأكد من حذف '${task.title}' من قائمة المهام المتسلسلة؟") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteTask(task.id)
            taskToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text("تأكيد الحذف")
        }
      },
      dismissButton = {
        TextButton(onClick = { taskToDelete = null }) {
          Text("إلغاء")
        }
      }
    )
  }

  // Dialog: Global CPAGrip & Proxy Settings
  if (showGlobalSettingsDialog) {
    GlobalSettingsDialog(
      currentConfig = currentConfig,
      onDismiss = { showGlobalSettingsDialog = false },
      onSave = { updated ->
        viewModel.updateTaskConfig(updated)
        showGlobalSettingsDialog = false
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskItemCard(
  task: TaskItem,
  taskIndex: Int,
  totalTasks: Int,
  isActive: Boolean,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onDuplicateClick: () -> Unit,
  onMoveUp: (() -> Unit)?,
  onMoveDown: (() -> Unit)?,
  userAgents: List<String>
) {
  val clipboardManager = LocalClipboardManager.current
  val statusColor = when {
    task.isRunning -> StatusBlue
    task.isCompleted -> StatusGreen
    else -> StatusYellow
  }
  val statusText = when {
    task.isRunning -> "⚡ قيد التنفيذ الآن"
    task.isCompleted -> "✓ مكتمل (${task.completedRepeats}/${task.taskRepeatCount})"
    else -> "⏳ بالانتظار (Pending)"
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("task_card_$taskIndex"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isActive) PrimaryPurpleLight.copy(alpha = 0.35f) else SurfaceLight
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      width = if (isActive) 1.5.dp else 1.dp,
      brush = androidx.compose.ui.graphics.SolidColor(if (isActive) PrimaryPurple else SurfaceBorder)
    )
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Top row: Task Number, Title & Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(PrimaryPurple)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "#${taskIndex + 1}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Text(
            text = task.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }

        // Status Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
          Text(
            text = statusText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
          )
        }
      }

      // Offer URL display
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF1E1C24))
          .padding(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "رابط العرض (Offer URL):",
              fontSize = 9.sp,
              color = Color(0xFFD0BCFF),
              fontWeight = FontWeight.Bold
            )
            Text(
              text = task.offerUrl,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          IconButton(
            onClick = { clipboardManager.setText(AnnotatedString(task.offerUrl)) },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.ContentCopy,
              contentDescription = "Copy URL",
              tint = Color(0xFFD0BCFF),
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      // Parameter Badges FlowRow
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Referrer Badge
        BadgeChip(
          icon = Icons.Outlined.Shuffle,
          text = if (task.useRandomReferrer) "🎲 Referrer عشوائي" else "🔗 ${task.referrerBaseUrl.take(22)}...",
          color = PrimaryPurpleDark
        )

        // User-Agent Badge
        val uaLabel = userAgents.getOrNull(task.selectedUserAgentIndex)?.substringBefore(" (") ?: "User-Agent"
        BadgeChip(
          icon = Icons.Outlined.PhoneAndroid,
          text = uaLabel,
          color = StatusBlue
        )

        // Mode Badge
        BadgeChip(
          icon = Icons.Outlined.Timer,
          text = "${task.selectedMode.displayName} (${task.browserDurationSeconds}s)",
          color = PrimaryPurple
        )

        // Repeats Badge
        BadgeChip(
          icon = Icons.Outlined.Repeat,
          text = "${task.taskRepeatCount} تكرار",
          color = StatusGreen
        )
      }

      // Action Buttons Row: Move Up / Down, Duplicate, Edit, Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Reordering & Duplicate
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (onMoveUp != null) {
            IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Outlined.ArrowUpward, contentDescription = "Move Up", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
          }
          if (onMoveDown != null) {
            IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Outlined.ArrowDownward, contentDescription = "Move Down", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
          }
          IconButton(onClick = onDuplicateClick, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Duplicate Task", tint = TextSecondary, modifier = Modifier.size(18.dp))
          }
        }

        // Edit and Delete Buttons (Requested by User)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onEditClick,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("edit_task_btn_$taskIndex"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple)
          ) {
            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("تعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onDeleteClick,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("delete_task_btn_$taskIndex"),
            colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
          ) {
            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text("حذف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
private fun BadgeChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String,
  color: Color
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.12f))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
      Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
  }
}

@Composable
private fun TaskEditDialog(
  title: String,
  initialTask: TaskItem,
  userAgents: List<String>,
  onDismiss: () -> Unit,
  onSave: (TaskItem) -> Unit
) {
  var taskTitle by remember { mutableStateOf(initialTask.title) }
  var offerUrl by remember { mutableStateOf(initialTask.offerUrl) }
  var useRandomReferrer by remember { mutableStateOf(initialTask.useRandomReferrer) }
  var customReferrerUrl by remember { mutableStateOf(initialTask.referrerBaseUrl) }
  var selectedUaIndex by remember { mutableIntStateOf(initialTask.selectedUserAgentIndex) }
  var selectedMode by remember { mutableStateOf(initialTask.selectedMode) }
  var taskRepeats by remember { mutableFloatStateOf(initialTask.taskRepeatCount.toFloat()) }
  var browserDuration by remember { mutableFloatStateOf(initialTask.browserDurationSeconds.toFloat()) }

  var utmSource by remember { mutableStateOf(initialTask.utmSource) }
  var utmMedium by remember { mutableStateOf(initialTask.utmMedium) }
  var utmCampaign by remember { mutableStateOf(initialTask.utmCampaign) }
  var utmContent by remember { mutableStateOf(initialTask.utmContent) }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          onSave(
            initialTask.copy(
              title = taskTitle.ifBlank { "CPA Task" },
              offerUrl = offerUrl.trim(),
              useRandomReferrer = useRandomReferrer,
              referrerBaseUrl = customReferrerUrl.trim(),
              selectedUserAgentIndex = selectedUaIndex,
              selectedMode = selectedMode,
              taskRepeatCount = taskRepeats.toInt(),
              browserDurationSeconds = browserDuration.toInt(),
              utmSource = utmSource.trim(),
              utmMedium = utmMedium.trim(),
              utmCampaign = utmCampaign.trim(),
              utmContent = utmContent.trim()
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Outlined.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text("حفظ المهمة", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    },
    title = {
      Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Task Title
        OutlinedTextField(
          value = taskTitle,
          onValueChange = { taskTitle = it },
          label = { Text("اسم المهمة (Task Title)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple)
        )

        // Offer URL
        OutlinedTextField(
          value = offerUrl,
          onValueChange = { offerUrl = it },
          label = { Text("رابط العرض (CPA Offer URL)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple)
        )

        // Referrer Section (Random or Custom)
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = PrimaryPurpleLight.copy(alpha = 0.3f))
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("🎲 Referrer عشوائي تلقائي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurpleDark)
                Text("توليد مواقع إحالة عشوائية (Google / Facebook / X / Bing)", fontSize = 10.sp, color = TextSecondary)
              }
              Switch(
                checked = useRandomReferrer,
                onCheckedChange = { useRandomReferrer = it },
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurpleLight)
              )
            }

            if (!useRandomReferrer) {
              OutlinedTextField(
                value = customReferrerUrl,
                onValueChange = { customReferrerUrl = it },
                label = { Text("رابط الإحالة المخصص (Custom Referrer)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple)
              )
            }
          }
        }

        // User Agent Selection
        Text("اختر User-Agent للمتصفح:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        userAgents.forEachIndexed { idx, ua ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(if (selectedUaIndex == idx) PrimaryPurpleLight.copy(alpha = 0.5f) else Color.White)
              .clickable { selectedUaIndex = idx }
              .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = selectedUaIndex == idx,
              onClick = { selectedUaIndex = idx },
              colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
            )
            Text(text = ua, fontSize = 11.sp, color = TextPrimary)
          }
        }

        // Mode Selection
        Text("اختر مود التشغيل الذكي (Mode):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        AutomationMode.values().forEach { mode ->
          val isSel = selectedMode == mode
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSel) PrimaryPurpleLight.copy(alpha = 0.4f) else Color.White)
              .border(1.dp, if (isSel) PrimaryPurple else OutlineColor, RoundedCornerShape(10.dp))
              .clickable { selectedMode = mode }
              .padding(8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = isSel,
                onClick = { selectedMode = mode },
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
              )
              Column {
                Text(text = mode.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = mode.shortDesc, fontSize = 10.sp, color = TextSecondary)
              }
            }
          }
        }

        // Sliders: Task Repeats & Duration
        Column {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("عدد تكرار المهمة (Repeats):", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Text("${taskRepeats.toInt()}x مرات", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
          }
          Slider(
            value = taskRepeats,
            onValueChange = { taskRepeats = it },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(thumbColor = PrimaryPurple, activeTrackColor = PrimaryPurple)
          )
        }

        Column {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("مدة فتح المتصفح (Browser Duration):", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Text("${browserDuration.toInt()} ثانية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
          }
          Slider(
            value = browserDuration,
            onValueChange = { browserDuration = it },
            valueRange = 20f..120f,
            colors = SliderDefaults.colors(thumbColor = PrimaryPurple, activeTrackColor = PrimaryPurple)
          )
        }

        // UTM Tags
        Text("معلمات UTM الإعلانية:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedTextField(
            value = utmSource,
            onValueChange = { utmSource = it },
            label = { Text("utm_source", fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          )
          OutlinedTextField(
            value = utmCampaign,
            onValueChange = { utmCampaign = it },
            label = { Text("utm_campaign", fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          )
        }
      }
    }
  )
}

@Composable
private fun GlobalSettingsDialog(
  currentConfig: com.example.model.TaskConfig,
  onDismiss: () -> Unit,
  onSave: (com.example.model.TaskConfig) -> Unit
) {
  var cpaUserId by remember { mutableStateOf(currentConfig.cpaGripUserId) }
  var cpaKey by remember { mutableStateOf(currentConfig.cpaGripKey) }
  var autoRotateProxy by remember { mutableStateOf(currentConfig.autoRotateProxyEachCycle) }
  var proxyListUrl by remember { mutableStateOf(currentConfig.proxyListUrl) }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          onSave(
            currentConfig.copy(
              cpaGripUserId = cpaUserId.trim(),
              cpaGripKey = cpaKey.trim(),
              autoRotateProxyEachCycle = autoRotateProxy,
              proxyListUrl = proxyListUrl.trim()
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
      ) {
        Text("حفظ الإعدادات العامة")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("إلغاء") }
    },
    title = { Text("الإعدادات العامة للتحقق والبروكسي", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("CPAGrip Lead RSS Verification:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        OutlinedTextField(
          value = cpaUserId,
          onValueChange = { cpaUserId = it },
          label = { Text("CPAGrip User ID") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
          value = cpaKey,
          onValueChange = { cpaKey = it },
          label = { Text("CPAGrip API Key") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("تدوير البروكسي تلقائياً لكل دورة", fontSize = 11.sp, fontWeight = FontWeight.Medium)
          Switch(
            checked = autoRotateProxy,
            onCheckedChange = { autoRotateProxy = it }
          )
        }

        OutlinedTextField(
          value = proxyListUrl,
          onValueChange = { proxyListUrl = it },
          label = { Text("Asocks Proxy List URL") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    }
  )
}
