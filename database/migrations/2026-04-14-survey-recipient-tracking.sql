USE [SURVEY_SYSTEM_DEV];
GO

CREATE TABLE [dbo].[Survey_Recipient] (
    [recipient_id] INT IDENTITY(1,1) NOT NULL,
    [survey_id] INT NOT NULL,
    [student_id] INT NOT NULL,
    [assigned_at] DATETIME NOT NULL
        CONSTRAINT [DF_SurveyRecipient_AssignedAt] DEFAULT (GETDATE()),
    [opened_at] DATETIME NULL,
    [submitted_at] DATETIME NULL,
    CONSTRAINT [PK_Survey_Recipient] PRIMARY KEY ([recipient_id]),
    CONSTRAINT [UQ_SurveyRecipient_SurveyStudent] UNIQUE ([survey_id], [student_id]),
    CONSTRAINT [FK_SurveyRecipient_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]),
    CONSTRAINT [FK_SurveyRecipient_Student] FOREIGN KEY ([student_id]) REFERENCES [dbo].[Student]([user_id])
);
GO

INSERT INTO [dbo].[Survey_Recipient] ([survey_id], [student_id], [assigned_at], [opened_at], [submitted_at])
SELECT DISTINCT
    s.[survey_id],
    st.[user_id],
    GETDATE(),
    CASE WHEN sr.[submitted_at] IS NOT NULL THEN sr.[submitted_at] ELSE NULL END,
    sr.[submitted_at]
FROM [dbo].[Survey] s
INNER JOIN [dbo].[Survey_Assignment] sa ON sa.[survey_id] = s.[survey_id]
INNER JOIN [dbo].[Student] st ON st.[status] = 'ACTIVE'
LEFT JOIN [dbo].[Survey_Response] sr ON sr.[survey_id] = s.[survey_id] AND sr.[student_id] = st.[user_id]
WHERE s.[lifecycle_state] IN ('PUBLISHED', 'CLOSED', 'ARCHIVED')
  AND sa.[evaluator_type] = 'STUDENT'
  AND (
        sa.[subject_type] = 'ALL'
        OR (sa.[subject_type] = 'DEPARTMENT' AND sa.[subject_value] = st.[dept_id])
      );
GO
