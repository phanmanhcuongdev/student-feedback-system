IF OBJECT_ID(N'[dbo].[Survey_Recipient]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_SurveyRecipient_Student_Submitted_Survey'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey_Recipient]')
   )
BEGIN
    CREATE INDEX [IX_SurveyRecipient_Student_Submitted_Survey]
    ON [dbo].[Survey_Recipient] ([student_id], [submitted_at], [survey_id])
    INCLUDE ([assigned_at], [opened_at]);
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Survey_StudentVisibility'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey]')
   )
BEGIN
    CREATE INDEX [IX_Survey_StudentVisibility]
    ON [dbo].[Survey] ([lifecycle_state], [hidden], [end_date], [start_date])
    INCLUDE ([created_by], [title], [description], [title_vi], [title_en], [description_vi], [description_en], [source_lang], [is_auto_translated]);
END
GO
