#!/bin/env python

""" Collaborative web-based Git mergetool.

This is a script to be used as a Git mergetool.
See: man git-mergetool
     http://schacon.github.com/git/git-mergetool.html
    
This script uploads the conflicted file to a server where multiple people
can collaboratively solve the merge.
"""

import sys
import os
import urllib
import urllib2
import subprocess as sub
import webbrowser

# MERGE_URL is where the collabmerge web application is running:
#
# This is just a temporary place, it might not be always up.
MERGE_URL = "http://antti.virtuallypreinstalled.com/collabmerge"
#
# For testing the web app
#MERGE_URL = "http://localhost:8080/collabmerge"

COLLAB_MERGE_SESSION_FILE = '.collabmergesession'

def get_git_executable():
    """Get git executable name. Tested on Linux and Windows."""
    if sys.platform=='win32':
        return 'git.exe'
    return 'git'

GIT = get_git_executable()

# The parameters for git mergetool
BASE, LOCAL, REMOTE, MERGED = sys.argv[1:]

def read_file(filename):
    #with open(filename, 'r') as f:
    #	return f.read()
    f = open(filename, 'r')
    ret = f.read()
    f.close()
    return ret

def get_author(commit):
    #cmd = [GIT, 'show', '--format=format:%an', commit]
    cmd = GIT+' show --format=format:%an '+ commit
    p = sub.Popen(cmd, shell=True, stdout=sub.PIPE)
    return p.stdout.readline()
    
def get_email(commit):
    #cmd = [GIT, 'show', '--format=format:%ae', commit]
    cmd = GIT+' show --format=format:%ae '+ commit
    p = sub.Popen(cmd, shell=True, stdout=sub.PIPE)
    return p.stdout.readline()

def get_my_name():
    #cmd = [GIT, 'config', 'user.name']
    cmd = GIT+' config user.name'
    p = sub.Popen(cmd, shell=True, stdout=sub.PIPE)
    return p.stdout.read()
    
def get_my_email():
    #cmd = [GIT, 'config', 'user.email']
    cmd = GIT+' config user.email'
    p = sub.Popen(cmd, shell=True, stdout=sub.PIPE)
    return p.stdout.read()


def get_data():
    merge_head = read_file('.git/MERGE_HEAD').strip()
        
    merge_author = get_author(merge_head).strip()
    merge_email = get_email(merge_head).strip()
        
    my_name = get_my_name().strip()
    my_email = get_my_email().strip()
    
    merged_text = read_file(MERGED)
        
    return get_encoded_data(merged_text, merge_author, merge_email, my_name, my_email)

def get_encoded_data(merged_text, merge_author, merge_email, my_name, my_email):
    return urllib.urlencode({
        'initmergetext': merged_text,
        'filename': os.path.basename(MERGED),
        'author0': my_name,
        'author0email': my_email,
        'author1': merge_author,
        'author1email': merge_email})

def start_new_session(data):
    """Uploads the data. Returns auth key received from the server."""
    result = urllib2.urlopen(MERGE_URL+'/?initmerge', data)
    return result.readline().strip() # AuthKey
    
def update_existing_session(data, auth_key):
    result = urllib2.urlopen(MERGE_URL+'/?initmerge&auth='+auth_key, data)
    return result.readline().strip() # AuthKey

def download_merge_result(auth_key):
    mr = urllib2.urlopen(MERGE_URL+'/?getmerge&auth='+auth_key)
    success = mr.readline().strip()=='SUCCESS'
    if success:
        return mr.read()
    else:
        return None

def write_merge_result(merge_result):
    f = open(MERGED, 'w')
    f.write(merge_result)
    f.close()
    
def get_unfinished_merge():
    try:
        f = open(COLLAB_MERGE_SESSION_FILE)
        auth_key = f.readline().strip()
        f.close()
        return auth_key
    except IOError:
        return None

def create_unfinished_merge_file(auth_key):
    f = open(COLLAB_MERGE_SESSION_FILE, 'w')
    f.write(auth_key)
    f.close()
    
def cleanup():
    os.remove(COLLAB_MERGE_SESSION_FILE)

def main():
    try:
        data = get_data()
        unfinished = get_unfinished_merge()
        if unfinished is not None:
            print "Found an unfinished merge session: %s" % (unfinished,)
            answer = raw_input("Do you want to use the same session with this merge? y/N >")
            if not answer or answer[0].lower()!='y':
                unfinished = None
        
        # TODO: if unfinished, continue existing session
        
        print
        print "Uploading conflicted file %s..." % (MERGED,),
        auth_key = start_new_session(data);
        print "Uploaded"
        if unfinished is None:
            create_unfinished_merge_file(auth_key)
        print
        
        url = MERGE_URL+'/?auth='+auth_key
        if AUTO_LAUNCH_BROWSER:
            print "Opening conflict resolving tool."
            webbrowser.open(url)
        else:
            print "Point your browser to this URL to resolve the conflict:"
            print
            print url
        print
        
        result = download_merge_result(auth_key)
        
        if result is None:
            print "Merge cancelled."
            cleanup()
            return -1
        else:
            write_merge_result(result)
            print "Merge successful."
            cleanup()
            return 0
            
    except Exception, e:
        print "Error %s" %(e,)
        return -1



if __name__=='__main__':
    sys.exit(main())

