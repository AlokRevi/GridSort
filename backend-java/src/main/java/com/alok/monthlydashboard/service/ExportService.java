package com.alok.monthlydashboard.service;

import com.alok.monthlydashboard.dto.export.SystemExportResponse;
import com.alok.monthlydashboard.dto.export.SetupSnapshotResponse;

public interface ExportService {

    SystemExportResponse exportSystemState();

    SetupSnapshotResponse exportSetupSnapshot();
}
