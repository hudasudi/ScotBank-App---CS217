function confirmLogout() {
  const confirmAction = confirm("Are you sure you want to log out?");
  if(confirmAction) {
    window.location.href = "LoginPage.html";
  }
  // else do nothing if they cancel
}