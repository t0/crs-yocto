# Provides GIT_DESCRIBE: `git describe` for the top-level repository (the
# parent of poky/, the other layers, and crs-mkids), which transitively pins
# every build input via submodules.  A release tag on the top-level repo
# therefore names the entire image.
#
# Modelled on poky's metadata_scm.bbclass: evaluated once at parse time (:=),
# with [vardepvalue] so task signatures track the resulting string -- a new
# tag or commit reruns consuming tasks even when no recipe input changed.

def t0_git_describe(d):
    import oe.buildcfg
    path = os.path.dirname(d.getVar('COREBASE'))
    describe = oe.buildcfg.get_metadata_git_describe(path) or "unknown"
    # Tracked changes only (including submodule drift); untracked files in
    # the working tree are not detected.
    if oe.buildcfg.is_layer_modified(path):
        describe += "-dirty"
    return describe

GIT_DESCRIBE := "${@t0_git_describe(d)}"
GIT_DESCRIBE[vardepvalue] = "${GIT_DESCRIBE}"
