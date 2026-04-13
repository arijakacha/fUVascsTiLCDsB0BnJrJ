// NexusPlay Gaming Platform - Interactive JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Initialize all interactive features
    initializeNavigation();
    initializeSearch();
    initializeNotifications();
    initializeLiveMatches();
    initializeLeaderboard();
    initializeForumInteractions();
    initializeAnimations();
});

// Navigation functionality
function initializeNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    
    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all items
            navItems.forEach(nav => nav.classList.remove('active'));
            
            // Add active class to clicked item
            this.classList.add('active');
            
            // Simulate page navigation
            const pageName = this.querySelector('span').textContent;
            console.log(`Navigating to ${pageName}`);
            
            // Add a subtle animation
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 150);
        });
    });
}

// Search functionality
function initializeSearch() {
    const searchInput = document.querySelector('.search-bar input');
    const searchIcon = document.querySelector('.search-bar i');
    
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const query = e.target.value.toLowerCase();
            
            if (query.length > 2) {
                // Simulate search results
                console.log(`Searching for: ${query}`);
                searchIcon.style.color = 'var(--color-primary)';
            } else {
                searchIcon.style.color = 'var(--text-muted)';
            }
        });
        
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                const query = e.target.value;
                if (query) {
                    console.log(`Performing search for: ${query}`);
                    // Here you would typically make an API call
                }
            }
        });
    }
}

// Notification system
function initializeNotifications() {
    const notificationBell = document.querySelector('.notification-bell');
    const badge = document.querySelector('.badge');
    
    if (notificationBell) {
        notificationBell.addEventListener('click', function() {
            // Clear badge on click
            if (badge) {
                badge.style.display = 'none';
            }
            
            // Show notification panel (simulation)
            console.log('Opening notification panel');
            showNotificationPanel();
        });
    }
}

function showNotificationPanel() {
    // Create notification panel
    const panel = document.createElement('div');
    panel.className = 'notification-panel';
    panel.innerHTML = `
        <div class="notification-header">
            <h3>Notifications</h3>
            <button class="close-btn">&times;</button>
        </div>
        <div class="notification-list">
            <div class="notification-item">
                <div class="notification-content">
                    <strong>New match available!</strong>
                    <p>Your team has a match scheduled in 30 minutes</p>
                </div>
                <span class="notification-time">5m ago</span>
            </div>
            <div class="notification-item">
                <div class="notification-content">
                    <strong>Achievement unlocked!</strong>
                    <p>You've reached Diamond rank in League of Legends</p>
                </div>
                <span class="notification-time">1h ago</span>
            </div>
            <div class="notification-item">
                <div class="notification-content">
                    <strong>Team invitation</strong>
                    <p>Phoenix team invited you to join their roster</p>
                </div>
                <span class="notification-time">2h ago</span>
            </div>
        </div>
    `;
    
    // Add styles
    panel.style.cssText = `
        position: fixed;
        top: 70px;
        right: 20px;
        width: 350px;
        max-height: 400px;
        background: var(--bg-card);
        border: 1px solid var(--bg-border);
        border-radius: var(--radius-lg);
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        z-index: 1000;
        overflow: hidden;
    `;
    
    document.body.appendChild(panel);
    
    // Close functionality
    const closeBtn = panel.querySelector('.close-btn');
    closeBtn.addEventListener('click', () => {
        panel.remove();
    });
    
    // Close on outside click
    setTimeout(() => {
        document.addEventListener('click', function closePanel(e) {
            if (!panel.contains(e.target) && !notificationBell.contains(e.target)) {
                panel.remove();
                document.removeEventListener('click', closePanel);
            }
        });
    }, 100);
}

// Live matches simulation
function initializeLiveMatches() {
    const liveMatches = document.querySelectorAll('.match-card.live');
    
    liveMatches.forEach(match => {
        // Simulate live score updates
        setInterval(() => {
            const scores = match.querySelectorAll('.score');
            if (scores.length === 2) {
                const team1Score = parseInt(scores[0].textContent);
                const team2Score = parseInt(scores[1].textContent);
                
                // Randomly increment scores
                if (Math.random() > 0.7) {
                    scores[Math.floor(Math.random() * 2)].textContent = 
                        Math.floor(Math.random() * 2) + team1Score;
                }
            }
        }, 10000); // Update every 10 seconds
    });
}

// Leaderboard interactions
function initializeLeaderboard() {
    const leaderboardItems = document.querySelectorAll('.leaderboard-item');
    const gameSelector = document.querySelector('.game-selector');
    
    leaderboardItems.forEach(item => {
        item.addEventListener('click', function() {
            const playerName = this.querySelector('.player-name').textContent;
            console.log(`Viewing profile for: ${playerName}`);
            
            // Add click animation
            this.style.transform = 'scale(0.98)';
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 150);
        });
    });
    
    if (gameSelector) {
        gameSelector.addEventListener('change', function() {
            const selectedGame = this.value;
            console.log(`Filtering leaderboard by: ${selectedGame}`);
            
            // Simulate loading new data
            leaderboardItems.forEach(item => {
                item.style.opacity = '0.5';
                setTimeout(() => {
                    item.style.opacity = '1';
                }, 300);
            });
        });
    }
}

// Forum interactions
function initializeForumInteractions() {
    const forumPosts = document.querySelectorAll('.forum-post');
    
    forumPosts.forEach(post => {
        post.addEventListener('click', function() {
            const title = this.querySelector('.post-title').textContent;
            console.log(`Opening forum post: ${title}`);
        });
        
        // Like functionality
        const likeButton = document.createElement('button');
        likeButton.className = 'like-btn';
        likeButton.innerHTML = '<i class="fas fa-thumbs-up"></i>';
        likeButton.style.cssText = `
            background: none;
            border: none;
            color: var(--text-muted);
            cursor: pointer;
            transition: color var(--transition-normal);
        `;
        
        const firstStat = post.querySelector('.post-stats .stat');
        if (firstStat) {
            firstStat.addEventListener('click', function(e) {
                e.stopPropagation();
                const icon = this.querySelector('i');
                const currentCount = parseInt(this.textContent.trim());
                
                if (icon.classList.contains('fas')) {
                    icon.classList.remove('fas');
                    icon.classList.add('far');
                    this.style.color = 'var(--text-muted)';
                    this.innerHTML = `<i class="far fa-thumbs-up"></i> ${currentCount - 1}`;
                } else {
                    icon.classList.remove('far');
                    icon.classList.add('fas');
                    this.style.color = 'var(--color-primary)';
                    this.innerHTML = `<i class="fas fa-thumbs-up"></i> ${currentCount + 1}`;
                }
            });
        }
    });
}

// Animations and micro-interactions
function initializeAnimations() {
    // Hero section animation
    const heroContent = document.querySelector('.hero-content');
    if (heroContent) {
        heroContent.style.opacity = '0';
        heroContent.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            heroContent.style.transition = 'all 0.8s ease';
            heroContent.style.opacity = '1';
            heroContent.style.transform = 'translateY(0)';
        }, 300);
    }
    
    // Card hover effects
    const cards = document.querySelectorAll('.match-card, .forum-post, .stat-card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 100 * (index + 1));
    });
    
    // User menu dropdown
    const userMenu = document.querySelector('.user-menu');
    if (userMenu) {
        userMenu.addEventListener('click', function(e) {
            e.stopPropagation();
            console.log('Opening user menu dropdown');
            
            // Create dropdown menu
            const dropdown = document.createElement('div');
            dropdown.className = 'user-dropdown';
            dropdown.innerHTML = `
                <a href="#"><i class="fas fa-user"></i> Profile</a>
                <a href="#"><i class="fas fa-cog"></i> Settings</a>
                <a href="#"><i class="fas fa-trophy"></i> Achievements</a>
                <a href="#"><i class="fas fa-sign-out-alt"></i> Logout</a>
            `;
            
            dropdown.style.cssText = `
                position: absolute;
                top: 100%;
                right: 0;
                background: var(--bg-card);
                border: 1px solid var(--bg-border);
                border-radius: var(--radius-md);
                padding: var(--spacing-sm);
                min-width: 200px;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                z-index: 1000;
            `;
            
            dropdown.querySelectorAll('a').forEach(link => {
                link.style.cssText = `
                    display: flex;
                    align-items: center;
                    gap: var(--spacing-sm);
                    padding: var(--spacing-sm);
                    color: var(--text-primary);
                    text-decoration: none;
                    border-radius: var(--radius-sm);
                    transition: background var(--transition-normal);
                `;
                
                link.addEventListener('mouseover', () => {
                    link.style.background = 'rgba(99, 102, 241, 0.1)';
                });
                
                link.addEventListener('mouseout', () => {
                    link.style.background = 'none';
                });
            });
            
            this.style.position = 'relative';
            this.appendChild(dropdown);
            
            // Close on outside click
            setTimeout(() => {
                document.addEventListener('click', function closeDropdown(e) {
                    if (!userMenu.contains(e.target)) {
                        dropdown.remove();
                        document.removeEventListener('click', closeDropdown);
                    }
                });
            }, 100);
        });
    }
}

// Utility functions
function showLoadingState(element) {
    element.style.opacity = '0.5';
    element.style.pointerEvents = 'none';
}

function hideLoadingState(element) {
    element.style.opacity = '1';
    element.style.pointerEvents = 'auto';
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    
    toast.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        background: var(--bg-card);
        color: var(--text-primary);
        padding: var(--spacing-md) var(--spacing-lg);
        border-radius: var(--radius-md);
        border: 1px solid var(--bg-border);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
        z-index: 1000;
        animation: slideIn 0.3s ease;
    `;
    
    if (type === 'success') {
        toast.style.borderColor = 'var(--color-success)';
    } else if (type === 'error') {
        toast.style.borderColor = 'var(--color-error)';
    }
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .notification-panel .notification-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--spacing-md);
        border-bottom: 1px solid var(--bg-border);
    }
    
    .notification-panel .notification-header h3 {
        margin: 0;
        font-size: var(--font-size-lg);
    }
    
    .notification-panel .close-btn {
        background: none;
        border: none;
        color: var(--text-muted);
        font-size: var(--font-size-xl);
        cursor: pointer;
    }
    
    .notification-panel .notification-list {
        max-height: 300px;
        overflow-y: auto;
    }
    
    .notification-panel .notification-item {
        display: flex;
        justify-content: space-between;
        padding: var(--spacing-md);
        border-bottom: 1px solid var(--bg-border);
        transition: background var(--transition-normal);
    }
    
    .notification-panel .notification-item:hover {
        background: rgba(99, 102, 241, 0.05);
    }
    
    .notification-panel .notification-content strong {
        display: block;
        margin-bottom: var(--spacing-xs);
    }
    
    .notification-panel .notification-content p {
        margin: 0;
        font-size: var(--font-size-sm);
        color: var(--text-secondary);
    }
    
    .notification-panel .notification-time {
        font-size: var(--font-size-xs);
        color: var(--text-muted);
        white-space: nowrap;
    }
`;

document.head.appendChild(style);
