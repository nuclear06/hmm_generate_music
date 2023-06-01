new Vue({
    el: '#app',
    data() {
        return {
            username: "Guest",
            tableData: null,
            formData: {
                rhythm: 0,    //拍子
                speed: 120,   //速度
                key: '', //ABCDEF..
                mention: 50, //情绪
                checkList: [],  //乐器
                soundFile: [],  //声音文件

                recordFile: null,
                soundUrl: "", //用于校验

            },
            recordBtnType: "primary",
            playBtnType: 'primary',
            minute: "00",
            second: "00",
            ms: "00",

            recordInterval: null,
            recordStart: false,
            recordPause: false,
            recordIcon: "el-icon-microphone",
            playBtnIcon: "el-icon-video-play",
            recordPlaying: false,
            recordPlayPause: false,
            recordPlayFinish: false,
            uploadFile: false, //录音还是上传

            showTable: false,
            showForm: true,

            soundUploadUrl: "/server",

            rules: {
                rhythm: [{required: true, message: "必填", trigger: 'blur'}],
                speed: [
                    {validator: this.validateNonZero, trigger: 'blur'},
                    {required: true, message: "必填", trigger: 'blur'},
                    {type: 'number', message: '速度必须为数字值'}
                ],
                mention: [{required: true, message: "必填", trigger: 'blur'}],
                key: [{required: true, message: "必选", trigger: 'blur'}],
                checkList: [{required: true, message: "至少选择一个乐器!", trigger: 'blur'}],
                soundUrl: [
                    {required: true, message: '请上传音频文件', trigger: 'blur'},
                ],
                recordFile: [{required: true, message: "请录音", trigger: 'blur'}],

            },
            recorder: null,


        }
    },
    methods: {
        validateNonZero(rule, value, callback) {
            if (value === 0) {
                callback(new Error('数字不能为0'));
            } else {
                callback();
            }
        },
        //侧边栏切换
        formChange(index) {
            if (index == 1) {
                this.showTable = false;
                const loading = this.$loading({
                    lock: true,
                    text: 'Loading',
                    background: 'rgba(255, 255, 255, 0.7)'
                });
                setTimeout(() => {
                    this.showForm = true;
                    loading.close();
                }, 500);
            } else if (index == 2) {
                //显示历史记录界面
                this.getTabledata()
                this.showForm = false;
                const loading = this.$loading({
                    lock: true,
                    text: 'Loading',
                    background: 'rgba(255, 255, 255, 0.7)'
                });
                setTimeout(() => {
                    this.showTable = true;
                    loading.close();
                }, 500);
            } else {
                console.log(index);
            }
        },

        //提交表单
        submitForm(formName) {
            let vm = this;
            this.$refs[formName].validate((valid) => {
                console.log(this.formData)
                if (valid) {
                    var postData = new FormData();
                    if (vm.uploadFile) {
                        vm.$refs.upload.submit();   //el-upload的方法
                        vm.formData.soundFile = []
                        vm.formData.soundUrl = ""
                    } else {
                        let form = this.formData;
                        postData.append('file', vm.formData.recordFile);
                        for (const data in form) {
                            if (form[data] !== null || form[data] !== "") {
                                if (data === 'soundUrl' || data === 'recordFile') continue;
                                postData.append(data, form[data]);
                            }
                        }
                        axios.post(this.soundUploadUrl, postData).then(res => {
                            this.dealUploadResp(res);
                        })
                    }

                } else {
                    return false;
                }
            });
        },
        dealUploadResp(resp) {
            let code = resp.data.code;
            if (code === 200) {
                this.$message({message: "上传成功", type: "success"})
                axios.get('/download/' + resp.data.data, {responseType: 'arraybuffer'})
                    .then((music) => {
                        console.log("这是下载..")
                        let file = music.headers['file-name']
                        let blob = new Blob([music.data], {type: 'audio/mpeg'});
                        this.download(blob, file)
                    })
            } else if (code === 400) {
                console.log(resp.data.message)
                this.$message({message: "服务器繁忙", type: "error"})
            } else {
                console.log(resp.data.message)
                this.$message({message: resp.data.msg, type: "error"})
            }
        },
        uploadToServer(item) {//el-upload的上传
            // console.log(item)
            let postData = new FormData();
            let form = this.formData;
            postData.append('file', item.file)
            for (const data in form) {
                if (form[data] !== null || form[data] !== "") {
                    if (data === 'soundUrl' || data === 'recordFile') continue;
                    postData.append(data, form[data]);
                }
            }
            console.log("postData")
            console.log(postData)
            axios.post(this.soundUploadUrl, postData).then(res => {
                this.dealUploadResp(res)
            })
        },
        //表单中文件变化
        handleChange(file, fileList) {
            console.log(fileList)
            if (file.size / (1024 * 1024) > 20) {  // 限制文件大小
                this.$message.warning(`当前限制文件大小不能大于20Mb`)
                if (fileList.length > 1) {
                    this.formData.soundFile = fileList.slice(0, fileList.length - 1)
                } else {
                    this.formData.soundFile = []
                }
                return false
            }
            if (fileList.length > 1) this.formData.soundFile = fileList.slice(1);
            this.formData.soundUrl = URL.createObjectURL(file.raw);
        },
        removeSoundFile(file, fileList) {
            console.log(file);
            this.formData.soundUrl = "";
        },
        exitUser() {
            axios.get('/user/exit');
            let s = window.location.href;
            let reg = /https?:\/\/.*?\//;
            let url = s.match(reg);
            window.location.href = url + 'login'
        },
        recordSound() {
            let _this = this;
            if (this.recordStart === true) {
                if (this.recordPause === true) {//恢复动作
                    this.recordPause = false;
                    this.recordIcon = "el-icon-video-pause";
                    this.recordBtnType = "warning";
                    this.recorder.resume();
                    console.log('录音恢复')
                    this.$message('录音已恢复');
                    this.showTime();
                } else {//暂停动作
                    this.recordPause = true;
                    this.recordIcon = "el-icon-microphone";
                    this.recordBtnType = "primary";
                    clearInterval(this.recordInterval);
                    this.recorder.pause();
                    console.log('录音暂停')
                    this.$message("录音已暂停")
                }
            } else {
                Recorder.getPermission().then(() => {
                    _this.recorder = new Recorder({
                        sampleBits: 16, // 采样位数，支持 8 或 16，默认是16
                        sampleRate: 48000, // 采样率，支持 11025、16000、22050、24000、44100、48000，根据浏览器默认值，我的chrome是48000
                        numChannels: 1
                    });
                    _this.recorder.onplayend = () => {
                        this.recordPlayPause = true;
                        this.recorder.resumePlay();
                        this.playBtnType = "primary";
                        this.playBtnIcon = "el-icon-video-play";
                        this.$message('录音播放结束')
                        this.recordPlayFinish = true;
                        clearInterval(this.recordInterval)
                    };
                    console.log('开始录音');
                    _this.recorder.start();
                    _this.recordStart = true;
                    this.showTime();
                    this.recordIcon = "el-icon-video-pause";
                    this.recordBtnType = "warning";
                }, (error) => {
                    _this.$message({message: '获取权限失败', type: 'error'})
                });
            }
        },

        stopRecord() {
            this.recorder.stop()
            this.recordPause = false;
            this.recordStart = false;
            this.minute = '00'
            this.second = '00'
            this.ms = '00'
            this.recordIcon = "el-icon-microphone";
            this.recordBtnType = "primary";

            this.$message({message: '录音已结束!', type: 'success'})
            clearInterval(this.recordInterval);
            const blob = this.recorder.getWAVBlob()// 获取wav格式音频数据
            // 此处获取到blob对象后需要设置fileName满足当前项目上传需求，其它项目可直接传把blob作为file塞入formData
            const newbolb = new Blob([blob], {type: 'audio/wav'})
            const fileOfBlob = new File([newbolb], new Date().getTime() + '.wav')
            this.formData.recordFile = fileOfBlob;
        },
        showTime() {
            this.recordInterval = setInterval(() => {
                let time = this.recorder.duration;
                let sTime = time.toString();

                let _minute = Math.floor((time / 60) % 60).toString()
                let _second = Math.floor(time % 60).toString()

                this.minute = _minute.length < 2 ? '0' + _minute : _minute;
                this.second = _second.length < 2 ? '0' + _second : _second;

                let index = sTime.indexOf('.') + 1;
                this.ms = sTime.substring(index, index + 2)
            }, 130);
        },
        showPlayTime() {
            this.recordInterval = setInterval(() => {
                let time = this.recorder.getPlayTime();
                let sTime = time.toString();

                let _minute = Math.floor((time / 60) % 60).toString()
                let _second = Math.floor(time % 60).toString()

                this.minute = _minute.length < 2 ? '0' + _minute : _minute;
                this.second = _second.length < 2 ? '0' + _second : _second;

                let index = sTime.indexOf('.') + 1;
                this.ms = sTime.substring(index, index + 2)
            }, 130);
        },
        playRecord() {
            if (this.recordPlayFinish) return;
            if (this.recordPlaying === true) {
                if (this.recordPlayPause === true) {//恢复事件
                    this.recordPlayPause = false;
                    this.recorder.resumePlay();
                    this.playBtnType = "warning";
                    this.playBtnIcon = "el-icon-video-pause";
                    this.$message('开始播放录音')
                    this.showPlayTime();
                } else {
                    this.recordPlayPause = true;
                    this.recorder.stopPlay();
                    this.playBtnType = "primary";
                    this.playBtnIcon = "el-icon-video-play";
                    this.$message('暂停播放录音')
                    clearInterval(this.recordInterval)
                }
            } else {
                console.log(this.player)
                this.recordPlaying = true;
                this.recorder.play()
                this.playBtnType = "warning";
                this.playBtnIcon = "el-icon-video-pause";
                this.$message('开始播放录音')
                this.showPlayTime();
            }
        },
        downloadRecord() {
            this.recorder.downloadWAV("Record" + this.getCurrentDate());
            this.$message({message: '已生成下载任务', type: 'success'});
        },
        deleteRecord() {
            this.formData.recordFile = null;
            this.recordPlaying = false
            this.recordPlayPause = false
            this.recordPlayFinish = false
            this.recorder.destroy();
            this.minute = '00'
            this.second = '00'
            this.ms = '00'
            clearInterval(this.recordInterval)
            this.$message('已删除录音');
        },
        getCurrentDate() {
            let myDate = new Date();
            let year = myDate.getFullYear();
            let month = myDate.getMonth();
            let day = myDate.getDay();
            return year + '/' + month + '/' + day;
        },
        downloadOriginFile(index, row) {
            axios.get("/download/person/" + (parseInt(index) + 1) + "?origin=1", {responseType: 'arraybuffer'})
                .then(resp => {
                    console.log(resp)

                    let file = resp.headers['file-name']
                    if (file === null) file = "file.unknown"
                    let type = 'audio/mpeg';
                    if (/.*?\.wav/.test(file)) type = 'audio/wav';
                    let blob = new Blob([resp.data], {type: type});
                    this.download(blob, file)
                })
        },
        downloadGenerateMusic(index, row) {
            axios.get("/download/person/" + (parseInt(index) + 1) + "?origin=0", {responseType: 'arraybuffer'})
                .then(resp => {
                    let file = resp.headers['file-name']
                    if (file === null) file = "file.unknown"
                    let type = 'audio/mpeg';
                    if (/.*?\.wav/.test(file)) type = 'audio/wav';
                    let blob = new Blob([resp.data], {type: type});
                    this.download(blob, file)
                })
        },
        getTabledata() {
            let dealBool = (item) => {
                if (item) {
                    return "√";
                } else {
                    return "X"
                }
            }
            axios.get("/user/data").then((data) => {
                let _data = data.data;
                if (_data.code === 200) {
                    let history = _data.history
                    history.forEach((item, index, array) => {
                        item.date = new Date(item.date).toLocaleString()
                        if (item.rhythm === 0) {
                            item.rhythm = "3/4拍"
                        } else {
                            item.rhythm = "4/4拍"
                        }
                        item.piano = dealBool(item.piano)
                        item.chordPiano = dealBool(item.chordPiano)
                        item.guitar = dealBool(item.guitar)
                        item.chordGuitar = dealBool(item.chordGuitar)
                        item.drum = dealBool(item.drum)
                        item.bass = dealBool(item.bass)
                    })
                    this.tableData = history
                } else {
                    this.$message({message: "网络错误,获取数据失败!", type: "error"});
                    console.log(_data)
                }
            })
        },
        download(blob, file_name) {
            if (typeof file_name === "undefined") {
                this.$message({message: "未找到文件!", type: "error"})
                return;
            }
            const blobUrl = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = blobUrl;
            let date = new Date();
            let y = date.getFullYear();
            let M = date.getMonth();
            let d = date.getDay();
            let h = date.getHours();
            let m = date.getMinutes();
            a.download = `Music[${y}.${M}.${d}-${h}.${m}].mp3`;
            if (file_name !== null) a.download = file_name;
            document.body.appendChild(a);
            this.$message({message: "已获取到下载文件", type: "success"})
            a.click();
            window.URL.revokeObjectURL(blobUrl);
        },
    },
    mounted() {
        let _this = this;
        axios.interceptors.response.use(
            (res) => res, // 成功的请求返回处理
            (err) => { // 异常的请求返回处理
                const data = {
                    message: err.response.data.message || '网络异常',
                    code: err.response.status !== 200 ? err.response.status : err.response.data.errCode
                }
                console.log(data)
                _this.$message({message: data.message, type: "error"})
                throw data;
            }
        );
        this.getTabledata()
        axios.get("/user/data").then(resp => {
            this.username = resp.data.username
        })
    }
})