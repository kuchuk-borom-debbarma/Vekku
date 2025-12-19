export interface CreateContentRequest {
    text: string;
    type: 'TEXT' | 'MARKDOWN';
}

export interface Content {
    id: string;
    text: string;
    type: 'TEXT' | 'MARKDOWN';
    userId: string;
    created: number;
    updated: number;
}

const API_BASE_URL = 'http://localhost:8080/api';

export const api = {
    createContent: async (data: CreateContentRequest): Promise<Content> => {
        const token = localStorage.getItem('accessToken');
        const response = await fetch(`${API_BASE_URL}/content`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error('Failed to create content');
        }

        return response.json();
    }
};
