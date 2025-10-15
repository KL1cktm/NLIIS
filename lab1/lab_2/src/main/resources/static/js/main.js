// Этот код выполнится, когда вся страница будет загружена
document.addEventListener('DOMContentLoaded', (event) => {

    // Находим нашу форму
    const form = document.querySelector('form');
    if (form) {
        // Добавляем обработчик события на отправку формы
        form.addEventListener('submit', () => {
            // Находим кнопку внутри формы
            const button = form.querySelector('button');
            if (button) {
                // Меняем текст и делаем кнопку неактивной
                button.textContent = 'Анализ...';
                button.disabled = true;
            }
        });
    }
});