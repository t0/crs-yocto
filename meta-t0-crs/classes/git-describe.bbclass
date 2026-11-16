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
    # Not oe.buildcfg.get_metadata_git_describe(), which runs git under
    # pseudo when a fakeroot task (do_package_qa, do_install, ...) reparses
    # the recipe. There git believes it is root, refuses the user's
    # repository ("detected dubious ownership"), and the helper's empty
    # result changes this value mid-build ("the basehash value changed").
    # is_layer_modified() already unloads pseudo the same way.
    try:
        describe, _ = bb.process.run('PSEUDO_UNLOAD=1 git describe --tags', cwd=path)
    except bb.process.ExecutionError as e:
        bb.fatal("git describe failed in %s: %s" % (path, e))
    describe = describe.strip()
    # Tracked changes only (including submodule drift); untracked files in
    # the working tree are not detected.
    if oe.buildcfg.is_layer_modified(path):
        describe += "-dirty"
    return describe

GIT_DESCRIBE := "${@t0_git_describe(d)}"
GIT_DESCRIBE[vardepvalue] = "${GIT_DESCRIBE}"
