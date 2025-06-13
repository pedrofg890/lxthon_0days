const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081';

export async function registerTeacher(teacherDTO) {
    const response = await fetch(`${API_URL}/teachers`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(teacherDTO)
    });

    let responseData = null;

    if (!response.ok) {
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            responseData = await response.json();
        } else {
            responseData = await response.text();
        }
        throw new Error(responseData.message || responseData);
    }

    try {
        responseData = await response.json();
    } catch (error) {
        console.warn("Resposta sem corpo JSON");
    }

    return responseData;
}

export async function getAllTeachers() {
    const response = await fetch(`${API_URL}/teachers`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error('Failed to fetch teachers');
    }
    return data._embedded ? data._embedded.teacherDTOList : [];
}