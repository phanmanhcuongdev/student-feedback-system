import SurveysDetailPage from "./features/surveys/pages/SurveyDetailPage.tsx";
import SurveysListPage from "./features/surveys/pages/SurveysPage.tsx";
import { Navigate, Route, Routes } from "react-router-dom";

function App() {
  return (
      <Routes>
          <Route path="/" element={<Navigate to="/surveys" replace />} />
          <Route path="/surveys" element={<SurveysListPage />} />
          <Route path="/surveys/:id" element={<SurveysDetailPage />} />
          <Route path="*" element={<Navigate to="/surveys" replace />} />
      </Routes>
  );
}

export default App
