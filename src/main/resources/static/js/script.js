document.addEventListener("DOMContentLoaded", function() {
    const form = document.querySelector("#registerForm");

    if (!form) return;

    const password = document.querySelector("#password");
    const confirmPassword = document.querySelector("#confirm_password");

    form.addEventListener("submit", (event) => {
        if(password.value !== confirmPassword.value) {
            event.preventDefault();
            alert("Passwords do not match");
        }
    });
});