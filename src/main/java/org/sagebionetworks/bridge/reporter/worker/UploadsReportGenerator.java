package org.sagebionetworks.bridge.reporter.worker;

import static java.util.stream.Collectors.counting;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.sagebionetworks.bridge.reporter.helper.BridgeHelper;
import org.sagebionetworks.bridge.reporter.request.ReportType;
import org.sagebionetworks.bridge.rest.model.Study;
import org.sagebionetworks.bridge.rest.model.Upload;

public class UploadsReportGenerator implements ReportGenerator {
    
    private BridgeHelper bridgeHelper;
    
    @Autowired
    @Qualifier("ReporterHelper")
    public final void setBridgeHelper(BridgeHelper bridgeHelper) {
        this.bridgeHelper = bridgeHelper;
    }

    @Override
    public Report generate(BridgeReporterRequest request, Study study) throws IOException {
        DateTime startDateTime = request.getStartDateTime();
        DateTime endDateTime = request.getEndDateTime();
        String scheduler = request.getScheduler();
        ReportType scheduleType = request.getScheduleType();

        String reportId = scheduler + scheduleType.getSuffix();
        
        Map<String, Integer> data = new HashMap<>();
        String studyId = study.getIdentifier();

        // get all uploads for this studyid
        List<Upload> uploadsForStudy = bridgeHelper.getUploadsForStudy(studyId, startDateTime, endDateTime);

        // aggregate and grouping by upload status
        uploadsForStudy.stream()
                .collect(Collectors.groupingBy(Upload::getStatus, counting()))
                .forEach((status, cnt) -> data.put(status.toString(), cnt.intValue()));

        return new Report.Builder().withStudyId(studyId).withReportId(reportId).withDate(startDateTime.toLocalDate())
                .withReportData(data).build();
    }
}
