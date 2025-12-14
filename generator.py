import os
import random
import subprocess
import sys
from datetime import datetime, timedelta

def run_git_command(command, cwd):
    """Executes a git command in the specified directory."""
    try:
        subprocess.run(
            command,
            cwd=cwd,
            check=True,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.PIPE,
            shell=True
        )
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {command}")
        print(e.stderr.decode())
        sys.exit(1)

def generate_commits(repo_path, file_name, total_commits, start_date, end_date):
    # Convert string dates to datetime objects
    try:
        start = datetime.strptime(start_date, "%Y-%m-%d")
        end = datetime.strptime(end_date, "%Y-%m-%d")
    except ValueError:
        print("Error: Dates must be in YYYY-MM-DD format.")
        return

    if start > end:
        print("Error: Start date cannot be after end date.")
        return

    # Calculate total days in the range
    delta_days = (end - start).days + 1
    
    print(f"Generating {total_commits} commits between {start_date} and {end_date}...")

    full_file_path = os.path.join(repo_path, file_name)

    # Ensure the repository path exists
    if not os.path.exists(os.path.join(repo_path, ".git")):
        print(f"Error: {repo_path} is not a valid git repository.")
        return

    commits_created = 0

    while commits_created < total_commits:
        # Pick a random day within the range
        random_day_offset = random.randint(0, delta_days - 1)
        current_date = start + timedelta(days=random_day_offset)
        
        # Randomize the time slightly to avoid identical timestamps
        random_hour = random.randint(0, 23)
        random_minute = random.randint(0, 59)
        random_second = random.randint(0, 59)
        
        commit_date = current_date.replace(
            hour=random_hour, minute=random_minute, second=random_second
        )
        
        # Format date for Git (ISO 8601 format recommended)
        date_str = commit_date.strftime("%Y-%m-%d %H:%M:%S")

        # Modify the file content to ensure git detects a change
        with open(full_file_path, "a") as f:
            f.write(f"Commit on {date_str}\n")

        # Git commands
        # 1. Stage the file
        run_git_command(f'git add "{file_name}"', repo_path)
        
        # 2. Commit with custom date environment variables
        # We set both GIT_AUTHOR_DATE and GIT_COMMITTER_DATE to ensure GitHub graphs pick it up correctly.
        env_vars = f'GIT_AUTHOR_DATE="{date_str}" GIT_COMMITTER_DATE="{date_str}"'
        commit_msg = f"Backdated commit: {date_str}"
        
        # Different syntax for Windows (set) vs Unix (env var prefix)
        if os.name == 'nt': # Windows
            command = f'set {env_vars} && git commit -m "{commit_msg}"'
        else: # Linux/Mac
            command = f'{env_vars} git commit -m "{commit_msg}"'
            
        run_git_command(command, repo_path)
        
        commits_created += 1
        print(f"[{commits_created}/{total_commits}] Committed on {date_str}")

    print("\nSuccess! Commits generated.")
    print("Run 'git push' to update your remote repository.")

if __name__ == "__main__":
    print("--- Git History Generator ---")
    
    # User Inputs
    r_path = input("Enter Repo Path (absolute path): ").strip()
    f_name = input("Enter File Name to modify (e.g., contribution.txt): ").strip()
    
    try:
        num = int(input("Enter Total Number of Commits: "))
    except ValueError:
        print("Invalid number.")
        sys.exit(1)
        
    s_date = input("Enter Start Date (YYYY-MM-DD): ").strip()
    e_date = input("Enter End Date (YYYY-MM-DD): ").strip()

    generate_commits(r_path, f_name, num, s_date, e_date)