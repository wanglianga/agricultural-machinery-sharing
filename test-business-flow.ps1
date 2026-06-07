# 农机共享平台业务主链路测试脚本
# 使用 PowerShell 调用 API 测试完整业务流程

$baseUrl = "http://localhost:8081/api"
$headers = @{"Content-Type" = "application/json"}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  农机共享平台 - 业务主链路测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤 1: 创建补贴政策（农服中心）
Write-Host "[步骤 1] 创建补贴政策..." -ForegroundColor Yellow
$subsidyPolicy = @{
    policyName = "2026年夏季作业补贴"
    subsidyPerMu = 50.00
    fuelSubsidyPerLiter = 1.20
    crossVillageApplicable = $true
    crossVillageExtraSubsidy = 100.00
    active = $true
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/subsidy-policies" -Method Post -Body $subsidyPolicy -Headers $headers
    $subsidyPolicyId = $response.id
    Write-Host "  ✓ 补贴政策创建成功，ID: $subsidyPolicyId" -ForegroundColor Green
} catch {
    Write-Host "  ✗ 补贴政策创建失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 2: 录入地块（种植大户）
Write-Host "[步骤 2] 种植大户录入地块信息..." -ForegroundColor Yellow
$field = @{
    fieldName = "张家村东头大田地"
    location = "张家村东头"
    village = "张家村"
    town = "李家镇"
    area = 50.50
    cropType = "小麦"
    soilType = "壤土"
    irrigationMethod = "喷灌"
    contactName = "张种植"
    contactPhone = "13800138001"
    growerId = 1
    remark = "土地肥沃，适合机械化作业"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/fields" -Method Post -Body $field -Headers $headers
    $fieldId = $response.id
    Write-Host "  ✓ 地块创建成功，ID: $fieldId" -ForegroundColor Green
    Write-Host "    地块名称: $($response.fieldName)" -ForegroundColor Gray
    Write-Host "    地块面积: $($response.area) 亩" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 地块创建失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 3: 录入机具（合作社）
Write-Host "[步骤 3] 合作社录入机具信息..." -ForegroundColor Yellow
$machine = @{
    machineNo = "JH-2026-001"
    machineType = "收割机"
    model = "约翰迪尔 S760"
    plateNumber = "京A12345"
    hourMeter = 1250.5
    operatorName = "李机手"
    operatorPhone = "13900139001"
    operatorId = 2
    serviceArea = "李家镇,王家镇"
    pricePerMu = 80.00
    cooperativeId = 1
    available = $true
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/machines" -Method Post -Body $machine -Headers $headers
    $machineId = $response.id
    Write-Host "  ✓ 机具创建成功，ID: $machineId" -ForegroundColor Green
    Write-Host "    机具型号: $($response.model)" -ForegroundColor Gray
    Write-Host "    作业价格: $($response.pricePerMu) 元/亩" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 机具创建失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 4: 创建作业预约（种植大户）
Write-Host "[步骤 4] 种植大户提交作业预约..." -ForegroundColor Yellow
$appointment = @{
    fieldId = $fieldId
    growerId = 1
    workType = "收割"
    appointmentDate = "2026-06-15"
    startTime = "08:00:00"
    endTime = "18:00:00"
    crossVillage = $false
    remark = "请尽早安排，小麦已成熟"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method Post -Body $appointment -Headers $headers
    $appointmentId = $response.id
    Write-Host "  ✓ 预约创建成功，ID: $appointmentId" -ForegroundColor Green
    Write-Host "    作业类型: $($response.workType)" -ForegroundColor Gray
    Write-Host "    预约日期: $($response.appointmentDate)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 预约创建失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 5: 合作社派机排程
Write-Host "[步骤 5] 合作社派机排程..." -ForegroundColor Yellow
$schedule = @{
    cooperativeId = 1
    machineId = $machineId
    operatorId = 2
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointments/$appointmentId/schedule" -Method Post -Body $schedule -Headers $headers
    Write-Host "  ✓ 排程成功" -ForegroundColor Green
    Write-Host "    安排机具 ID: $($response.machineId)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 排程失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 6: 从预约生成作业单
Write-Host "[步骤 6] 生成作业单..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/work-orders/from-appointment/$appointmentId" -Method Post -Headers $headers
    $workOrderId = $response.id
    Write-Host "  ✓ 作业单创建成功，ID: $workOrderId" -ForegroundColor Green
    Write-Host "    作业单号: $($response.orderNo)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 作业单创建失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 7: 农机手上报到田
Write-Host "[步骤 7] 农机手上报到田..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/work-orders/$workOrderId/arrive" -Method Post -Headers $headers
    Write-Host "  ✓ 到田上报成功" -ForegroundColor Green
    Write-Host "    到田时间: $($response.arriveTime)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 到田上报失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 8: 农机手开始作业
Write-Host "[步骤 8] 农机手开始作业..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/work-orders/$workOrderId/start" -Method Post -Headers $headers
    Write-Host "  ✓ 开始作业成功" -ForegroundColor Green
    Write-Host "    开始时间: $($response.startTime)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 开始作业失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 9: 农机手完工上报
Write-Host "[步骤 9] 农机手完工上报..." -ForegroundColor Yellow
$completeWork = @{
    actualArea = 49.80
    completionPhotoUrl = "https://example.com/photo/wo001.jpg"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/work-orders/$workOrderId/complete" -Method Post -Body $completeWork -Headers $headers
    Write-Host "  ✓ 完工上报成功" -ForegroundColor Green
    Write-Host "    实测面积: $($response.actualArea) 亩" -ForegroundColor Gray
    Write-Host "    作业时长: $($response.workHours) 小时" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 完工上报失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 10: 上传油票（农机手）
Write-Host "[步骤 10] 农机手上传油票..." -ForegroundColor Yellow
$fuelTicket = @{
    workOrderId = $workOrderId
    operatorId = 2
    fuelStation = "中石化李家镇加油站"
    fuelType = "柴油"
    fuelAmount = 120.50
    unitPrice = 7.50
    totalAmount = 903.75
    ticketPhotoUrl = "https://example.com/fuel/ft001.jpg"
    fuelTime = "2026-06-15T12:30:00"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/fuel-tickets" -Method Post -Body $fuelTicket -Headers $headers
    $fuelTicketId = $response.id
    Write-Host "  ✓ 油票上传成功，ID: $fuelTicketId" -ForegroundColor Green
    Write-Host "    加油量: $($response.fuelAmount) 升" -ForegroundColor Gray
    Write-Host "    总金额: $($response.totalAmount) 元" -ForegroundColor Gray
    Write-Host "    是否异常: $($response.abnormal)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 油票上传失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 11: 上传一张异常油票（用于测试油票异常处理）
Write-Host "[步骤 11] 上传一张异常油票（油量异常大）..." -ForegroundColor Yellow
$abnormalFuelTicket = @{
    workOrderId = $workOrderId
    operatorId = 2
    fuelStation = "不明加油站"
    fuelType = "柴油"
    fuelAmount = 9999.99
    unitPrice = 7.50
    totalAmount = 74999.93
    ticketPhotoUrl = "https://example.com/fuel/ft002.jpg"
    fuelTime = "2026-06-15T14:00:00"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/fuel-tickets" -Method Post -Body $abnormalFuelTicket -Headers $headers
    $abnormalFuelTicketId = $response.id
    Write-Host "  ✓ 异常油票上传成功，ID: $abnormalFuelTicketId" -ForegroundColor Green
    Write-Host "    加油量: $($response.fuelAmount) 升" -ForegroundColor Gray
    Write-Host "    是否异常: $($response.abnormal)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 异常油票上传失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 12: 种植大户面积确认（制造争议场景）
Write-Host "[步骤 12] 种植大户面积确认（制造争议场景）..." -ForegroundColor Yellow
$confirmArea = @{
    confirmedArea = 48.00
    areaConsistent = $false
    disputeReason = "实测面积与上报面积有差异，相差约1.8亩，需要重新丈量"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/work-orders/$workOrderId/confirm-area" -Method Post -Body $confirmArea -Headers $headers
    Write-Host "  ✓ 面积确认提交成功（争议场景）" -ForegroundColor Green
    Write-Host "    确认面积: $($response.growerConfirmedArea) 亩" -ForegroundColor Gray
    Write-Host "    面积一致: $($response.areaConsistent)" -ForegroundColor Gray
    Write-Host "    争议原因: $($response.areaDisputeReason)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 面积确认失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 13: 农服中心处理面积争议
Write-Host "[步骤 13] 农服中心处理面积争议..." -ForegroundColor Yellow
$resolveDispute = @{
    finalArea = 49.00
    resolutionRemark = "经农服中心现场重新丈量，最终确定面积为49亩，双方认可"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/work-orders/$workOrderId/resolve-dispute" -Method Post -Body $resolveDispute -Headers $headers
    Write-Host "  ✓ 面积争议处理完成" -ForegroundColor Green
    Write-Host "    最终面积: $($response.finalSettledArea) 亩" -ForegroundColor Gray
    Write-Host "    处理备注: $($response.remark)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 面积争议处理失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 14: 农服中心审核油票（审核正常油票）
Write-Host "[步骤 14] 农服中心审核正常油票..." -ForegroundColor Yellow
$verifyNormal = @{
    verifierId = 3
    isNormal = $true
    abnormalReason = $null
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/fuel-tickets/$fuelTicketId/verify" -Method Post -Body $verifyNormal -Headers $headers
    Write-Host "  ✓ 正常油票审核通过" -ForegroundColor Green
    Write-Host "    是否已审核: $($response.verified)" -ForegroundColor Gray
    Write-Host "    是否异常: $($response.abnormal)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 油票审核失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 15: 农服中心处理异常油票
Write-Host "[步骤 15] 农服中心处理异常油票..." -ForegroundColor Yellow
$verifyAbnormal = @{
    verifierId = 3
    isNormal = $false
    abnormalReason = "加油量严重超出正常作业范围，油票来源不明，不予认可"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/fuel-tickets/$abnormalFuelTicketId/verify" -Method Post -Body $verifyAbnormal -Headers $headers
    Write-Host "  ✓ 异常油票处理完成" -ForegroundColor Green
    Write-Host "    是否已审核: $($response.verified)" -ForegroundColor Gray
    Write-Host "    是否异常: $($response.abnormal)" -ForegroundColor Gray
    Write-Host "    异常原因: $($response.abnormalReason)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 异常油票处理失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 16: 核算补贴与结算
Write-Host "[步骤 16] 核算补贴与结算..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/settlements/calculate/$workOrderId" -Method Post -Headers $headers
    $settlementId = $response.id
    Write-Host "  ✓ 结算核算完成，ID: $settlementId" -ForegroundColor Green
    Write-Host "    结算单号: $($response.settlementNo)" -ForegroundColor Gray
    Write-Host "    结算面积: $($response.settledArea) 亩" -ForegroundColor Gray
    Write-Host "    作业时长: $($response.workHours) 小时" -ForegroundColor Gray
    Write-Host "    服务费: $($response.serviceFee) 元" -ForegroundColor Gray
    Write-Host "    燃油费用: $($response.fuelCost) 元" -ForegroundColor Gray
    Write-Host "    作业补贴: $($response.operationSubsidy) 元" -ForegroundColor Gray
    Write-Host "    燃油补贴: $($response.fuelSubsidy) 元" -ForegroundColor Gray
    Write-Host "    跨村补贴: $($response.crossVillageSubsidy) 元" -ForegroundColor Gray
    Write-Host "    补贴合计: $($response.totalSubsidy) 元" -ForegroundColor Gray
    Write-Host "    种植户应付: $($response.growerPayable) 元" -ForegroundColor Gray
    Write-Host "    合作社应收: $($response.cooperativeReceivable) 元" -ForegroundColor Gray
    Write-Host "    农机手应收: $($response.operatorReceivable) 元" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 结算核算失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 17: 农服中心审核结算单
Write-Host "[步骤 17] 农服中心审核结算单..." -ForegroundColor Yellow
$approveSettlement = @{
    approverId = 3
    remark = "经审核，结算数据准确无误，同意支付"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/settlements/$settlementId/approve" -Method Post -Body $approveSettlement -Headers $headers
    Write-Host "  ✓ 结算单审核通过" -ForegroundColor Green
    Write-Host "    审核人: $($response.approvedBy)" -ForegroundColor Gray
    Write-Host "    审核时间: $($response.approvedAt)" -ForegroundColor Gray
    Write-Host "    审核备注: $($response.approvalRemark)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 结算单审核失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤 18: 标记已支付
Write-Host "[步骤 18] 标记结算单已支付..." -ForegroundColor Yellow
$markPaid = @{
    paymentVoucher = "PAY202606150001"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/settlements/$settlementId/paid" -Method Post -Body $markPaid -Headers $headers
    Write-Host "  ✓ 结算单已标记为已支付" -ForegroundColor Green
    Write-Host "    支付凭证: $($response.paymentVoucher)" -ForegroundColor Gray
    Write-Host "    支付时间: $($response.paidAt)" -ForegroundColor Gray
    Write-Host "    当前状态: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ 标记支付失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  恭喜！业务主链路测试全部完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "测试覆盖的业务场景：" -ForegroundColor Yellow
Write-Host "  1. 农服中心 - 配置补贴政策" -ForegroundColor White
Write-Host "  2. 种植大户 - 录入地块信息" -ForegroundColor White
Write-Host "  3. 合作社 - 维护机具信息" -ForegroundColor White
Write-Host "  4. 种植大户 - 提交作业预约" -ForegroundColor White
Write-Host "  5. 合作社 - 派机排程" -ForegroundColor White
Write-Host "  6. 系统 - 生成作业单" -ForegroundColor White
Write-Host "  7. 农机手 - 到田上报" -ForegroundColor White
Write-Host "  8. 农机手 - 开始作业" -ForegroundColor White
Write-Host "  9. 农机手 - 完工上报" -ForegroundColor White
Write-Host "  10. 农机手 - 上传油票（正常）" -ForegroundColor White
Write-Host "  11. 农机手 - 上传油票（异常）" -ForegroundColor White
Write-Host "  12. 种植大户 - 面积确认（争议场景）" -ForegroundColor White
Write-Host "  13. 农服中心 - 处理面积争议" -ForegroundColor White
Write-Host "  14. 农服中心 - 审核正常油票" -ForegroundColor White
Write-Host "  15. 农服中心 - 处理异常油票" -ForegroundColor White
Write-Host "  16. 系统 - 自动核算补贴与结算" -ForegroundColor White
Write-Host "  17. 农服中心 - 审核结算单" -ForegroundColor White
Write-Host "  18. 财务 - 标记已支付" -ForegroundColor White
Write-Host ""
