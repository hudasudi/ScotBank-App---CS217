const transactions = [
    ["12-03-2025", "Online Shopping", -150.49, 4559.51, "09:42", "Payment", "Clothing"],
    ["11-03-2025", "Coffee", -10, 4710, "09:42", "Payment", "Clothing"],
    ["10-03-2025", "Movie Tickets", -40, 4720, "09:42", "Payment", "Clothing"],
    ["09-03-2025", "Mr John Smith", 700, 4760, "09:42", "Payment", "Clothing"],
    ["08-03-2025", "Car Payment", -300, 4060, "09:42", "Payment", "Clothing"],
    ["07-03-2025", "Online Shopping", -60, 4360, "09:42", "Payment", "Clothing"],
    ["06-03-2025", "Restaurant", -200, 4420, "09:42", "Payment", "Clothing"],
    ["05-03-2025", "Internet Bill", -50, 4620, "09:42", "Payment", "Clothing"],
    ["04-03-2025", "Gas Bill", -80, 4670],
    ["03-03-2025", "Electric Bill", -100, 4750],
    ["02-03-2025", "Groceries", -150, 4850],
    ["01-03-2025", "Salary", 5000, 5000]
];

let currentIndex = 0; // Tracks how many rows are displayed
const rowsPerPage = 5;
const tableBody = document.getElementById("table-body");
const loadMoreBtn = document.getElementById("load-more-button");


function showMoreRows() {
const nextIndex = Math.min(currentIndex + rowsPerPage, transactions.length);

for (let i = currentIndex; i < nextIndex; i++) {
    let row = document.createElement("tr");
    row.innerHTML = `
        <td>${transactions[i][0]}</td>
        <td>${transactions[i][1]}</td>
        <td>${transactions[i][2].toFixed(2)}</td>
        <td>${transactions[i][3].toFixed(2)}</td>
        <td><button class="view-btn">See Details</button></td>
    `;
    tableBody.appendChild(row);

    // Create a hidden row for details
    let detailsRow = document.createElement("tr");
    detailsRow.classList.add("details-row");
    detailsRow.style.display = "none"; // Initially hidden
    detailsRow.innerHTML = `
        <td colspan="5">
            <div class="details-content">
                <div class="details-item"><strong>Time:</strong> ${transactions[i][4]}<br></div>
                <div class="details-item"><strong>Transaction Type:</strong> ${transactions[i][5]}<br></div>
                <div class="details-item"><strong>Business Category:</strong> ${transactions[i][6]}<br></div>
            </div>
        </td>
    `;
    tableBody.appendChild(detailsRow);

    // Attach event listener to toggle details row
    row.querySelector(".view-btn").addEventListener("click", function () {
        detailsRow.style.display = detailsRow.style.display === "none" ? "table-row" : "none";
    });
}

currentIndex = nextIndex;

// Hide button when all rows are displayed
if (currentIndex >= transactions.length) {
    loadMoreBtn.style.display = "none";
}
}

// Load initial rows
showMoreRows();